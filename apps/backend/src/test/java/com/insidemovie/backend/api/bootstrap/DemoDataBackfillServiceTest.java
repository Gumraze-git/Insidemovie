package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountSeedReport;
import com.insidemovie.backend.api.auth.service.DemoAccountSeedService;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillService;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillService;
import com.insidemovie.backend.api.movie.service.MoviePosterAuditReport;
import com.insidemovie.backend.api.movie.service.MoviePosterAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataBackfillServiceTest {

    @Mock
    private DemoDataBackfillProperties properties;
    @Mock
    private DemoAccountSeedService demoAccountSeedService;
    @Mock
    private BoxOfficeService boxOfficeService;
    @Mock
    private MovieGenreBackfillService movieGenreBackfillService;
    @Mock
    private MovieMetadataBackfillService movieMetadataBackfillService;
    @Mock
    private MoviePosterAuditService moviePosterAuditService;
    @Mock
    private ReviewAiSeedService reviewAiSeedService;
    @Mock
    private MatchSeedService matchSeedService;

    @InjectMocks
    private DemoDataBackfillService demoDataBackfillService;

    @Test
    void runShouldAggregateReports() {
        DemoDataBackfillProperties.Review review = new DemoDataBackfillProperties.Review();
        review.setTargetPerAccount(20);
        review.setFixturePath("seed/demo-reviews.v1.jsonl");
        review.setMaxContentLen(260);
        DemoDataBackfillProperties.Match match = new DemoDataBackfillProperties.Match();
        match.setClosedTargetCount(8);
        match.setCurrentVoteTarget(10);

        when(properties.isIncludeAccounts()).thenReturn(true);
        when(properties.isIncludeBoxoffice()).thenReturn(false);
        when(properties.isIncludeGenres()).thenReturn(true);
        when(properties.isIncludeMetadata()).thenReturn(true);
        when(properties.isIncludePosterRefresh()).thenReturn(false);
        when(properties.isIncludeReviews()).thenReturn(true);
        when(properties.isIncludeMatches()).thenReturn(true);
        when(properties.getReview()).thenReturn(review);
        when(properties.getMatch()).thenReturn(match);

        when(demoAccountSeedService.seed(false)).thenReturn(DemoAccountSeedReport.builder()
                .totalDefinitions(35)
                .createdMembers(3)
                .updatedMembers(32)
                .createdEmotionSummaries(3)
                .updatedEmotionSummaries(32)
                .build());
        when(movieGenreBackfillService.backfill(false)).thenReturn(MovieGenreBackfillReport.builder()
                .requestedMovies(80).succeededMovies(70).failedMovies(2).ignoredMovies(8)
                .mappedGenreRows(120).initialGenreRows(0).finalGenreRows(120)
                .build());
        when(movieMetadataBackfillService.backfill(false)).thenReturn(MovieMetadataBackfillReport.builder()
                .requestedMovies(50).succeededMovies(40).failedMovies(3).ignoredMovies(7)
                .updatedPosterCount(25).updatedOverviewCount(20).updatedBackdropCount(18)
                .build());
        when(reviewAiSeedService.seed(false, review)).thenReturn(ReviewAiSeedReport.builder()
                .requestedReviews(600)
                .createdReviews(580)
                .skippedReviews(15)
                .failedReviews(5)
                .fixtureLoadedRows(600)
                .fixtureInvalidRows(0)
                .build());
        when(matchSeedService.seed(false, 8, 10)).thenReturn(MatchSeedReport.builder()
                .closedMatchesCreated(8).currentMatchesCreated(1).votesCreated(250).build());

        DemoDataBackfillReport report = demoDataBackfillService.run(false);

        assertThat(report.getAccountsCreated()).isEqualTo(3);
        assertThat(report.getGenreMapped()).isEqualTo(120);
        assertThat(report.getMetadataUpdatedPoster()).isEqualTo(25);
        assertThat(report.getReviewsCreated()).isEqualTo(580);
        assertThat(report.getReviewFixtureLoaded()).isEqualTo(600);
        assertThat(report.getReviewFixtureInvalid()).isZero();
        assertThat(report.getEmotionsCreated()).isEqualTo(580);
        assertThat(report.getMatchesClosedCreated()).isEqualTo(8);
        assertThat(report.getCurrentCreated()).isEqualTo(1);
        assertThat(report.getPosterAuditMatchedUpdated()).isZero();
    }

    @Test
    void runShouldExecutePosterRefreshWhenEnabled() {
        when(properties.isIncludeAccounts()).thenReturn(false);
        when(properties.isIncludeBoxoffice()).thenReturn(false);
        when(properties.isIncludeGenres()).thenReturn(false);
        when(properties.isIncludeMetadata()).thenReturn(false);
        when(properties.isIncludePosterRefresh()).thenReturn(true);
        when(properties.isIncludeReviews()).thenReturn(false);
        when(properties.isIncludeMatches()).thenReturn(false);
        when(properties.getPosterRefresh()).thenReturn(new DemoDataBackfillProperties.PosterRefresh());

        when(movieMetadataBackfillService.backfill(false)).thenReturn(MovieMetadataBackfillReport.builder()
                .requestedMovies(5).succeededMovies(4).failedMovies(1).ignoredMovies(0)
                .updatedPosterCount(3).updatedOverviewCount(1).updatedBackdropCount(1)
                .build());
        when(moviePosterAuditService.auditAndBackfill(false, false)).thenReturn(MoviePosterAuditReport.builder()
                .totalMovies(80)
                .targetMissingPosterMovies(50)
                .alreadyHasPoster(30)
                .kobisNoPosterSource(50)
                .kmdbNoResult(20)
                .kmdbResultNoPoster(0)
                .matchScoreBelowThreshold(10)
                .matchedUpdated(20)
                .failed(0)
                .build());

        DemoDataBackfillReport report = demoDataBackfillService.run(false);

        assertThat(report.getMetadataUpdatedPoster()).isEqualTo(3);
        assertThat(report.getPosterAuditTargetMissing()).isEqualTo(50);
        assertThat(report.getPosterAuditMatchedUpdated()).isEqualTo(20);
        assertThat(report.getPosterAuditKmdbNoResult()).isEqualTo(20);
        assertThat(report.getPosterAuditMatchScoreBelowThreshold()).isEqualTo(10);
    }
}
