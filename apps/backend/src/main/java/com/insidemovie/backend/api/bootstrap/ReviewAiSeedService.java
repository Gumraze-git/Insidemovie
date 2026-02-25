package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAiSeedService {

    private static final int FASTAPI_RETRY = 2;

    private final DemoAccountCatalogService demoAccountCatalogService;
    private final MemberRepository memberRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final DemoReviewFixtureLoader fixtureLoader;

    @Transactional
    public ReviewAiSeedReport seed(boolean dryRun, DemoDataBackfillProperties.Review reviewProperties) {
        DemoReviewFixtureLoadResult loadResult = fixtureLoader.load(
                reviewProperties.getFixturePath(),
                reviewProperties.getMaxContentLen()
        );

        Map<String, List<DemoReviewSeedRow>> rowsByAccountKey = loadResult.rows().stream()
                .collect(Collectors.groupingBy(DemoReviewSeedRow::accountKey));

        int requested = 0;
        int created = 0;
        int skipped = 0;
        int failed = 0;

        Map<String, Optional<Movie>> movieByKoficIdCache = new HashMap<>();

        for (DemoAccountCatalogService.DemoAccountDefinition definition
                : demoAccountCatalogService.getGeneralAccountDefinitions()) {
            List<DemoReviewSeedRow> accountRows = rowsByAccountKey.getOrDefault(definition.accountKey(), List.of());
            int targetPerAccount = Math.max(0, reviewProperties.getTargetPerAccount());
            if (targetPerAccount > 0 && accountRows.size() > targetPerAccount) {
                accountRows = accountRows.subList(0, targetPerAccount);
            }

            requested += accountRows.size();

            Optional<Member> memberOptional = memberRepository.findByEmail(definition.email());
            if (memberOptional.isEmpty()) {
                failed += accountRows.size();
                log.warn("[ReviewAiSeed] member not found email={} accountKey={}", definition.email(), definition.accountKey());
                continue;
            }
            Member member = memberOptional.get();

            for (DemoReviewSeedRow row : accountRows) {
                Optional<Movie> movieOptional = movieByKoficIdCache.computeIfAbsent(
                        row.movieKoficId(),
                        movieRepository::findByKoficId
                );

                if (movieOptional.isEmpty()) {
                    failed++;
                    log.warn("[ReviewAiSeed] movie not found by koficId={} accountKey={}",
                            row.movieKoficId(), row.accountKey());
                    continue;
                }
                Movie movie = movieOptional.get();

                if (reviewRepository.findByMemberAndMovie(member, movie).isPresent()) {
                    skipped++;
                    continue;
                }

                ReviewCreateDTO request = ReviewCreateDTO.builder()
                        .content(row.content())
                        .rating(row.rating())
                        .spoiler(row.spoiler())
                        .watchedAt(row.watchedAt())
                        .build();

                if (dryRun) {
                    created++;
                    continue;
                }

                if (createWithRetry(movie.getId(), request, member.getId())) {
                    created++;
                } else {
                    failed++;
                }
            }
        }

        return ReviewAiSeedReport.builder()
                .requestedReviews(requested)
                .createdReviews(created)
                .skippedReviews(skipped)
                .failedReviews(failed)
                .fixtureLoadedRows(loadResult.rows().size())
                .fixtureInvalidRows(loadResult.invalidRows())
                .build();
    }

    private boolean createWithRetry(Long movieId, ReviewCreateDTO request, Long userId) {
        for (int attempt = 1; attempt <= FASTAPI_RETRY + 1; attempt++) {
            try {
                reviewService.createReview(movieId, request, userId);
                return true;
            } catch (Exception e) {
                if (attempt > FASTAPI_RETRY) {
                    log.warn("[ReviewAiSeed] createReview failed movieId={} userId={} error={}",
                            movieId, userId, e.getMessage());
                }
            }
        }
        return false;
    }
}
