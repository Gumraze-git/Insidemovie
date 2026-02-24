package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountSeedReport;
import com.insidemovie.backend.api.auth.service.DemoAccountSeedService;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillService;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillService;
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
    private MovieGenreBackfillService movieGenreBackfillService;
    @Mock
    private MovieMetadataBackfillService movieMetadataBackfillService;
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
        review.setIncludeWebFallback(true);
        DemoDataBackfillProperties.Match match = new DemoDataBackfillProperties.Match();
        match.setClosedTargetCount(8);
        match.setCurrentVoteTarget(10);

        when(properties.isIncludeAccounts()).thenReturn(true);
        when(properties.isIncludeGenres()).thenReturn(true);
        when(properties.isIncludeMetadata()).thenReturn(true);
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
        when(reviewAiSeedService.seed(false, 20, true)).thenReturn(ReviewAiSeedReport.builder()
                .requestedReviews(600).createdReviews(580).skippedReviews(15).failedReviews(5).build());
        when(matchSeedService.seed(false, 8, 10)).thenReturn(MatchSeedReport.builder()
                .closedMatchesCreated(8).currentMatchesCreated(1).votesCreated(250).build());

        DemoDataBackfillReport report = demoDataBackfillService.run(false);

        assertThat(report.getAccountsCreated()).isEqualTo(3);
        assertThat(report.getGenreMapped()).isEqualTo(120);
        assertThat(report.getMetadataUpdatedPoster()).isEqualTo(25);
        assertThat(report.getReviewsCreated()).isEqualTo(580);
        assertThat(report.getEmotionsCreated()).isEqualTo(580);
        assertThat(report.getMatchesClosedCreated()).isEqualTo(8);
        assertThat(report.getCurrentCreated()).isEqualTo(1);
    }
}

