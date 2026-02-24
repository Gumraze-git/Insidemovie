package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountSeedReport;
import com.insidemovie.backend.api.auth.service.DemoAccountSeedService;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillService;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoDataBackfillService {

    private final DemoDataBackfillProperties properties;
    private final DemoAccountSeedService demoAccountSeedService;
    private final MovieGenreBackfillService movieGenreBackfillService;
    private final MovieMetadataBackfillService movieMetadataBackfillService;
    private final ReviewAiSeedService reviewAiSeedService;
    private final MatchSeedService matchSeedService;

    public DemoDataBackfillReport run(boolean dryRun) {
        DemoAccountSeedReport accountReport = DemoAccountSeedReport.builder()
                .totalDefinitions(0)
                .createdMembers(0)
                .updatedMembers(0)
                .createdEmotionSummaries(0)
                .updatedEmotionSummaries(0)
                .build();

        MovieGenreBackfillReport genreReport = MovieGenreBackfillReport.builder()
                .requestedMovies(0)
                .succeededMovies(0)
                .failedMovies(0)
                .ignoredMovies(0)
                .mappedGenreRows(0)
                .initialGenreRows(0)
                .finalGenreRows(0)
                .build();

        MovieMetadataBackfillReport metadataReport = MovieMetadataBackfillReport.builder()
                .requestedMovies(0)
                .succeededMovies(0)
                .failedMovies(0)
                .ignoredMovies(0)
                .updatedPosterCount(0)
                .updatedBackdropCount(0)
                .updatedOverviewCount(0)
                .build();

        ReviewAiSeedReport reviewReport = ReviewAiSeedReport.builder()
                .requestedReviews(0)
                .createdReviews(0)
                .skippedReviews(0)
                .failedReviews(0)
                .build();

        MatchSeedReport matchReport = MatchSeedReport.builder()
                .closedMatchesCreated(0)
                .currentMatchesCreated(0)
                .votesCreated(0)
                .build();

        if (properties.isIncludeAccounts()) {
            accountReport = demoAccountSeedService.seed(dryRun);
        }
        if (properties.isIncludeGenres()) {
            genreReport = movieGenreBackfillService.backfill(dryRun);
        }
        if (properties.isIncludeMetadata()) {
            metadataReport = movieMetadataBackfillService.backfill(dryRun);
        }
        if (properties.isIncludeReviews()) {
            reviewReport = reviewAiSeedService.seed(
                    dryRun,
                    properties.getReview().getTargetPerAccount(),
                    properties.getReview().isIncludeWebFallback()
            );
        }
        if (properties.isIncludeMatches()) {
            matchReport = matchSeedService.seed(
                    dryRun,
                    properties.getMatch().getClosedTargetCount(),
                    properties.getMatch().getCurrentVoteTarget()
            );
        }

        return DemoDataBackfillReport.builder()
                .accountsCreated(accountReport.getCreatedMembers())
                .accountsUpdated(accountReport.getUpdatedMembers())
                .genreMapped(genreReport.getMappedGenreRows())
                .metadataUpdatedPoster(metadataReport.getUpdatedPosterCount())
                .metadataUpdatedOverview(metadataReport.getUpdatedOverviewCount())
                .metadataUpdatedBackdrop(metadataReport.getUpdatedBackdropCount())
                .reviewsRequested(reviewReport.getRequestedReviews())
                .reviewsCreated(reviewReport.getCreatedReviews())
                .reviewsSkipped(reviewReport.getSkippedReviews())
                .reviewsFailed(reviewReport.getFailedReviews())
                .emotionsCreated(reviewReport.getCreatedReviews())
                .matchesClosedCreated(matchReport.getClosedMatchesCreated())
                .currentCreated(matchReport.getCurrentMatchesCreated())
                .votesCreated(matchReport.getVotesCreated())
                .build();
    }
}

