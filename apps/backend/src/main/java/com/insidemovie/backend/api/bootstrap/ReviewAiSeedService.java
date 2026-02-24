package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.KmdbMovieClient;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.model.KmdbMovieCandidate;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.infrastructure.kobis.model.KobisMovieInfo;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAiSeedService {

    private static final String DEFAULT_SYNOPSIS_MESSAGE = "시놉시스 준비 중입니다.";
    private static final int MAX_CONTENT_LEN = 260;
    private static final int FASTAPI_RETRY = 2;

    private final DemoAccountCatalogService demoAccountCatalogService;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final KobisMovieInfoClient kobisMovieInfoClient;
    private final KmdbMovieClient kmdbMovieClient;
    private final WebSynopsisClient webSynopsisClient;

    @Transactional
    public ReviewAiSeedReport seed(boolean dryRun, int targetPerAccount, boolean includeWebFallback) {
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            return ReviewAiSeedReport.builder()
                    .requestedReviews(0)
                    .createdReviews(0)
                    .skippedReviews(0)
                    .failedReviews(0)
                    .build();
        }

        List<DemoAccountCatalogService.DemoAccountDefinition> generalAccounts =
                demoAccountCatalogService.getGeneralAccountDefinitions();

        Random random = new Random(20260224L);
        Map<Long, String> synopsisCache = new ConcurrentHashMap<>();
        int requested = 0;
        int created = 0;
        int skipped = 0;
        int failed = 0;

        for (DemoAccountCatalogService.DemoAccountDefinition account : generalAccounts) {
            Optional<Member> memberOptional = memberRepository.findByEmail(account.email());
            if (memberOptional.isEmpty()) {
                log.warn("[ReviewAiSeed] member not found for demo account email={}", account.email());
                failed++;
                continue;
            }

            Member member = memberOptional.get();
            long existing = reviewRepository.countByMember(member);
            int need = Math.max(0, targetPerAccount - (int) existing);
            if (need == 0) {
                skipped += targetPerAccount;
                continue;
            }

            Set<Long> reviewedMovieIds = reviewRepository.findMovieIdsByMemberId(member.getId()).stream()
                    .collect(Collectors.toSet());

            List<Movie> candidates = movies.stream()
                    .filter(movie -> !reviewedMovieIds.contains(movie.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (candidates.isEmpty()) {
                skipped += need;
                continue;
            }

            Collections.shuffle(candidates, random);
            List<Movie> targets = candidates.stream().limit(need).toList();
            requested += targets.size();

            for (Movie movie : targets) {
                String content = synopsisCache.computeIfAbsent(
                        movie.getId(),
                        id -> generateReviewContent(movie, includeWebFallback)
                );
                ReviewCreateDTO request = ReviewCreateDTO.builder()
                        .content(content)
                        .rating(pickRating(random))
                        .spoiler(false)
                        .watchedAt(LocalDateTime.now().minusDays(1 + random.nextInt(120)))
                        .build();

                if (dryRun) {
                    created++;
                    continue;
                }

                boolean success = false;
                for (int attempt = 1; attempt <= FASTAPI_RETRY + 1; attempt++) {
                    try {
                        reviewService.createReview(movie.getId(), request, member.getId());
                        created++;
                        success = true;
                        break;
                    } catch (Exception e) {
                        if (attempt > FASTAPI_RETRY) {
                            failed++;
                            log.warn("[ReviewAiSeed] createReview failed memberId={} movieId={} error={}",
                                    member.getId(), movie.getId(), e.getMessage());
                        }
                    }
                }

                if (!success) {
                    skipped++;
                }
            }
        }

        return ReviewAiSeedReport.builder()
                .requestedReviews(requested)
                .createdReviews(created)
                .skippedReviews(skipped)
                .failedReviews(failed)
                .build();
    }

    private String generateReviewContent(Movie movie, boolean includeWebFallback) {
        String synopsis = extractSynopsis(movie, includeWebFallback);
        String normalized = normalizeSynopsis(synopsis);
        String review = normalized + " 연출과 배우의 합이 좋아 몰입해서 봤고 전체적으로 만족스러웠습니다.";
        if (review.length() <= MAX_CONTENT_LEN) {
            return review;
        }
        return review.substring(0, MAX_CONTENT_LEN);
    }

    private String extractSynopsis(Movie movie, boolean includeWebFallback) {
        String overview = safeText(movie.getOverview());
        if (!overview.isBlank() && !DEFAULT_SYNOPSIS_MESSAGE.equals(overview)) {
            return overview;
        }

        Optional<String> kmdbSynopsis = fetchKmdbSynopsisFromKobis(movie);
        if (kmdbSynopsis.isPresent()) {
            return kmdbSynopsis.get();
        }

        if (includeWebFallback) {
            Optional<String> web = webSynopsisClient.searchSynopsis(movie.getTitle(), movie.getTitleEn());
            if (web.isPresent()) {
                return web.get();
            }
        }

        return movie.getTitle() + "은 감정선이 뚜렷하고 완성도가 안정적인 작품이었습니다.";
    }

    private Optional<String> fetchKmdbSynopsisFromKobis(Movie movie) {
        String title = safeText(movie.getTitle());
        Integer year = movie.getReleaseDate() == null ? null : movie.getReleaseDate().getYear();
        String director = "";

        if (movie.getKoficId() != null && !movie.getKoficId().isBlank()) {
            Optional<KobisMovieInfo> info = kobisMovieInfoClient.fetchMovieInfo(movie.getKoficId());
            if (info.isPresent()) {
                KobisMovieInfo kobis = info.get();
                if (!safeText(kobis.title()).isBlank()) {
                    title = kobis.title();
                }
                if (kobis.productionYear() != null) {
                    year = kobis.productionYear();
                }
                if (kobis.directors() != null && !kobis.directors().isEmpty()) {
                    director = kobis.directors().get(0);
                }
            }
        }

        List<KmdbMovieCandidate> candidates = kmdbMovieClient.searchMovieCandidates(title, year, director, 5);
        if (candidates.isEmpty() && year != null) {
            candidates = kmdbMovieClient.searchMovieCandidates(title, null, director, 5);
        }
        if (candidates.isEmpty() && movie.getTitleEn() != null && !movie.getTitleEn().isBlank()) {
            candidates = kmdbMovieClient.searchMovieCandidates(movie.getTitleEn(), null, director, 5);
        }
        return candidates.stream()
                .map(KmdbMovieCandidate::overview)
                .map(this::safeText)
                .filter(text -> !text.isBlank())
                .findFirst();
    }

    private String normalizeSynopsis(String synopsis) {
        String text = safeText(synopsis).replaceAll("\\s+", " ").trim();
        if (text.isBlank()) {
            return "이 작품은 전개와 감정 표현의 균형이 좋았습니다.";
        }
        if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
            text = text + ".";
        }
        return text;
    }

    private String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace('\n', ' ').replace('\r', ' ');
    }

    private double pickRating(Random random) {
        double rating = 3.5 + (random.nextInt(16) * 0.1);
        return Math.round(rating * 10.0) / 10.0;
    }
}
