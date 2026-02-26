package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountSeedReport;
import com.insidemovie.backend.api.auth.service.DemoAccountSeedService;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillService;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillService;
import com.insidemovie.backend.api.movie.service.MoviePosterAuditReport;
import com.insidemovie.backend.api.movie.service.MoviePosterAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoDataBackfillService {

    private final DemoDataBackfillProperties properties;
    private final DemoAccountSeedService demoAccountSeedService;
    private final BoxOfficeService boxOfficeService;
    private final MovieGenreBackfillService movieGenreBackfillService;
    private final MovieMetadataBackfillService movieMetadataBackfillService;
    private final MoviePosterAuditService moviePosterAuditService;
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
                .fixtureLoadedRows(0)
                .fixtureInvalidRows(0)
                .build();

        MatchSeedReport matchReport = MatchSeedReport.builder()
                .closedMatchesCreated(0)
                .currentMatchesCreated(0)
                .votesCreated(0)
                .build();
        MoviePosterAuditReport posterAuditReport = MoviePosterAuditReport.builder()
                .totalMovies(0)
                .targetMissingPosterMovies(0)
                .alreadyHasPoster(0)
                .kobisNoPosterSource(0)
                .kmdbNoResult(0)
                .kmdbResultNoPoster(0)
                .matchScoreBelowThreshold(0)
                .matchedUpdated(0)
                .failed(0)
                .build();

        if (properties.isIncludeAccounts()) {
            accountReport = demoAccountSeedService.seed(dryRun);
        }
        if (properties.isIncludeBoxoffice()) {
            if (dryRun) {
                // Boxoffice external fetch는 dry-run에서 실행하지 않고 단계만 표시한다.
            } else {
                BoxOfficeRequestDTO request = BoxOfficeRequestDTO.builder()
                        .itemPerPage(10)
                        .weekGb("0")
                        .build();
                boxOfficeService.fetchAndStoreDailyBoxOffice(request);
                boxOfficeService.fetchAndStoreWeeklyBoxOffice(request);
            }
        }
        if (properties.isIncludeGenres()) {
            genreReport = movieGenreBackfillService.backfill(dryRun);
        }
        if (properties.isIncludeMetadata()) {
            metadataReport = movieMetadataBackfillService.backfill(dryRun);
        }
        if (properties.isIncludeReviews()) {
            reviewReport = reviewAiSeedService.seed(dryRun, properties.getReview());
        }
        if (properties.isIncludeMatches()) {
            matchReport = matchSeedService.seed(
                    dryRun,
                    properties.getMatch().getClosedTargetCount(),
                    properties.getMatch().getCurrentVoteTarget()
            );
        }
        if (properties.isIncludePosterRefresh()) {
            if (!properties.isIncludeMetadata()) {
                metadataReport = movieMetadataBackfillService.backfill(dryRun);
            }
            posterAuditReport = moviePosterAuditService.auditAndBackfill(
                    dryRun,
                    properties.getPosterRefresh().isIncludeDetails()
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
                .reviewFixtureLoaded(reviewReport.getFixtureLoadedRows())
                .reviewFixtureInvalid(reviewReport.getFixtureInvalidRows())
                .emotionsCreated(reviewReport.getCreatedReviews())
                .matchesClosedCreated(matchReport.getClosedMatchesCreated())
                .currentCreated(matchReport.getCurrentMatchesCreated())
                .votesCreated(matchReport.getVotesCreated())
                .posterAuditTargetMissing(posterAuditReport.getTargetMissingPosterMovies())
                .posterAuditMatchedUpdated(posterAuditReport.getMatchedUpdated())
                .posterAuditKmdbNoResult(posterAuditReport.getKmdbNoResult())
                .posterAuditMatchScoreBelowThreshold(posterAuditReport.getMatchScoreBelowThreshold())
                .build();
    }
}
