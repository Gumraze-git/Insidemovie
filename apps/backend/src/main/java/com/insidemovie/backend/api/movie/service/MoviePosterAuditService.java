package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.KmdbMovieClient;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.model.KmdbMovieCandidate;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.infrastructure.kobis.model.KobisMovieInfo;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoviePosterAuditService {

    private static final int MATCH_SCORE_THRESHOLD = 70;

    private final MovieRepository movieRepository;
    private final KobisMovieInfoClient kobisMovieInfoClient;
    private final KmdbMovieClient kmdbMovieClient;

    @Transactional
    public MoviePosterAuditReport auditAndBackfill(boolean dryRun) {
        List<Movie> movies = movieRepository.findAllByKoficIdIsNotNull();

        int totalMovies = 0;
        int targetMissingPosterMovies = 0;
        int alreadyHasPoster = 0;
        int kobisNoPosterSource = 0;
        int kmdbNoResult = 0;
        int kmdbResultNoPoster = 0;
        int matchScoreBelowThreshold = 0;
        int matchedUpdated = 0;
        int failed = 0;

        for (Movie movie : movies) {
            totalMovies++;

            if (!isBlank(movie.getPosterPath())) {
                alreadyHasPoster++;
                continue;
            }

            targetMissingPosterMovies++;
            kobisNoPosterSource++;

            try {
                Optional<KobisMovieInfo> infoOptional = kobisMovieInfoClient.fetchMovieInfo(movie.getKoficId());
                KobisMovieInfo info = infoOptional.orElse(null);

                String searchTitle = firstNonBlank(info == null ? null : info.title(), movie.getTitle());
                String titleEn = firstNonBlank(info == null ? null : info.titleEn(), movie.getTitleEn());
                Integer year = resolveYear(info, movie);
                String director = resolveDirector(info);

                List<KmdbMovieCandidate> candidates = searchCandidates(searchTitle, titleEn, year, director);
                if (candidates.isEmpty()) {
                    kmdbNoResult++;
                    continue;
                }

                Optional<KmdbMovieCandidate> matched = selectBestMatch(info, movie, candidates);
                if (matched.isEmpty()) {
                    matchScoreBelowThreshold++;
                    continue;
                }

                KmdbMovieCandidate candidate = matched.get();
                if (isBlank(candidate.posterPath())) {
                    kmdbResultNoPoster++;
                    continue;
                }

                if (!dryRun) {
                    movie.updatePosterPath(candidate.posterPath().trim());
                    if (isBlank(movie.getBackdropPath()) && !isBlank(candidate.backdropPath())) {
                        movie.updateBackDropPath(candidate.backdropPath().trim());
                    }
                    if (isBlank(movie.getOverview()) && !isBlank(candidate.overview())) {
                        movie.updateOverview(candidate.overview().trim());
                    }
                    if (Boolean.TRUE != movie.getIsMatched()) {
                        movie.setIsMatched(true);
                    }
                    movieRepository.save(movie);
                }
                matchedUpdated++;
            } catch (Exception e) {
                failed++;
                log.warn("[MoviePosterAudit] failed movieId={} koficId={} error={}",
                        movie.getId(), movie.getKoficId(), e.getMessage());
            }
        }

        return MoviePosterAuditReport.builder()
                .totalMovies(totalMovies)
                .targetMissingPosterMovies(targetMissingPosterMovies)
                .alreadyHasPoster(alreadyHasPoster)
                .kobisNoPosterSource(kobisNoPosterSource)
                .kmdbNoResult(kmdbNoResult)
                .kmdbResultNoPoster(kmdbResultNoPoster)
                .matchScoreBelowThreshold(matchScoreBelowThreshold)
                .matchedUpdated(matchedUpdated)
                .failed(failed)
                .build();
    }

    private Optional<KmdbMovieCandidate> selectBestMatch(KobisMovieInfo info, Movie movie, List<KmdbMovieCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }

        String kobisTitle = firstNonBlank(info == null ? null : info.title(), movie.getTitle());
        Integer kobisYear = resolveYear(info, movie);
        Set<String> kobisDirectors = normalizeNames(info == null ? List.of() : info.directors());

        KmdbMovieCandidate best = null;
        int bestScore = -1;
        boolean bestHasPoster = false;

        for (KmdbMovieCandidate candidate : candidates) {
            int score = scoreTitle(kobisTitle, candidate.title())
                    + scoreYear(kobisYear, candidate.productionYear())
                    + scoreDirectors(kobisDirectors, candidate.directors());
            boolean hasPoster = !isBlank(candidate.posterPath());
            if (score > bestScore || (score == bestScore && hasPoster && !bestHasPoster)) {
                bestScore = score;
                best = candidate;
                bestHasPoster = hasPoster;
            }
        }

        if (best == null || bestScore < MATCH_SCORE_THRESHOLD) {
            return Optional.empty();
        }
        return Optional.of(best);
    }

    private List<KmdbMovieCandidate> searchCandidates(String title, String titleEn, Integer year, String director) {
        List<KmdbMovieCandidate> merged = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        appendUnique(merged, seen, kmdbMovieClient.searchMovieCandidates(title, year, director, 5));
        if (year != null) {
            appendUnique(merged, seen, kmdbMovieClient.searchMovieCandidates(title, year, "", 5));
        }
        if (!isBlank(director)) {
            appendUnique(merged, seen, kmdbMovieClient.searchMovieCandidates(title, null, director, 5));
        }
        appendUnique(merged, seen, kmdbMovieClient.searchMovieCandidates(title, null, "", 5));
        if (!isBlank(titleEn)) {
            appendUnique(merged, seen, kmdbMovieClient.searchMovieCandidates(titleEn, null, "", 5));
        }
        return merged;
    }

    private void appendUnique(List<KmdbMovieCandidate> merged, Set<String> seen, List<KmdbMovieCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        for (KmdbMovieCandidate candidate : candidates) {
            String key = normalizeText(candidate.title()) + "|" + candidate.productionYear() + "|"
                    + (candidate.directors() == null || candidate.directors().isEmpty()
                    ? ""
                    : normalizeText(candidate.directors().get(0)));
            if (seen.add(key)) {
                merged.add(candidate);
            }
        }
    }

    private int scoreTitle(String sourceTitle, String candidateTitle) {
        if (isBlank(sourceTitle) || isBlank(candidateTitle)) {
            return 0;
        }
        String source = normalizeText(sourceTitle);
        String candidate = normalizeText(candidateTitle);
        if (source.equals(candidate)) {
            return 70;
        }
        if (source.contains(candidate) || candidate.contains(source)) {
            return 40;
        }
        return 0;
    }

    private int scoreYear(Integer sourceYear, Integer candidateYear) {
        if (sourceYear == null || candidateYear == null) {
            return 0;
        }
        return sourceYear.equals(candidateYear) ? 30 : 0;
    }

    private int scoreDirectors(Set<String> sourceDirectors, List<String> candidateDirectors) {
        if (sourceDirectors.isEmpty() || candidateDirectors == null || candidateDirectors.isEmpty()) {
            return 0;
        }
        Set<String> normalizedCandidates = normalizeNames(candidateDirectors);
        for (String source : sourceDirectors) {
            if (normalizedCandidates.contains(source)) {
                return 20;
            }
        }
        return 0;
    }

    private Set<String> normalizeNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptySet();
        }
        return names.stream()
                .map(this::normalizeText)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("!HS|!HE|<[^>]+>", " ")
                .replaceAll("\\([^)]*\\)|\\[[^\\]]*\\]", " ")
                .replaceAll("[:：\\-–_]", " ")
                .replaceAll("[\\s\\p{Punct}]+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String firstNonBlank(String primary, String fallback) {
        if (!isBlank(primary)) {
            return primary.trim();
        }
        if (!isBlank(fallback)) {
            return fallback.trim();
        }
        return "";
    }

    private Integer resolveYear(KobisMovieInfo info, Movie movie) {
        if (info != null && info.productionYear() != null) {
            return info.productionYear();
        }
        if (info != null && !isBlank(info.openDt())) {
            String digits = info.openDt().replaceAll("[^0-9]", "");
            if (digits.length() >= 4) {
                return Integer.parseInt(digits.substring(0, 4));
            }
        }
        if (movie.getReleaseDate() != null) {
            return movie.getReleaseDate().getYear();
        }
        return null;
    }

    private String resolveDirector(KobisMovieInfo info) {
        if (info == null || info.directors() == null || info.directors().isEmpty()) {
            return "";
        }
        return firstNonBlank(info.directors().get(0), "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
