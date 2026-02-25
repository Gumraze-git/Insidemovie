package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.api.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewAiSeedServiceTest {

    @Mock
    private DemoAccountCatalogService demoAccountCatalogService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewService reviewService;
    @Mock
    private DemoReviewFixtureLoader fixtureLoader;

    @InjectMocks
    private ReviewAiSeedService reviewAiSeedService;

    @Test
    void shouldCreateOnlyMissingReviewsPerGeneralAccount() {
        DemoAccountCatalogService.DemoAccountDefinition general = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-01", "general01@demo.insidemovie.local", "일반01", "일반 계정 01", "GENERAL",
                EmotionType.JOY, 0.6f, 0.1f, 0.1f, 0.1f, 0.1f
        );

        DemoReviewSeedRow row = DemoReviewSeedRow.builder()
                .accountKey("general-01")
                .movieKoficId("20261150")
                .rating(4.5)
                .spoiler(false)
                .watchedAt(LocalDateTime.parse("2025-01-04T11:07:00"))
                .content("테스트 리뷰")
                .build();

        DemoDataBackfillProperties.Review reviewProperties = new DemoDataBackfillProperties.Review();
        reviewProperties.setFixturePath("seed/demo-reviews.v1.jsonl");
        reviewProperties.setTargetPerAccount(20);
        reviewProperties.setMaxContentLen(260);

        Member member = Member.builder().id(11L).email(general.email()).isBanned(false).build();
        Movie movie = Movie.builder().id(1L).koficId("20261150").title("점보").build();

        when(demoAccountCatalogService.getGeneralAccountDefinitions()).thenReturn(List.of(general));
        when(fixtureLoader.load("seed/demo-reviews.v1.jsonl", 260))
                .thenReturn(new DemoReviewFixtureLoadResult(List.of(row), 0));
        when(memberRepository.findByEmail(general.email())).thenReturn(Optional.of(member));
        when(movieRepository.findByKoficId("20261150")).thenReturn(Optional.of(movie));
        when(reviewRepository.findByMemberAndMovie(member, movie)).thenReturn(Optional.empty());
        when(reviewService.createReview(eq(1L), any(), eq(11L))).thenReturn(1L);

        ReviewAiSeedReport report = reviewAiSeedService.seed(false, reviewProperties);

        assertThat(report.getRequestedReviews()).isEqualTo(1);
        assertThat(report.getCreatedReviews()).isEqualTo(1);
        assertThat(report.getSkippedReviews()).isZero();
        assertThat(report.getFailedReviews()).isZero();
        assertThat(report.getFixtureLoadedRows()).isEqualTo(1);
        assertThat(report.getFixtureInvalidRows()).isZero();
        verify(reviewService, times(1)).createReview(eq(1L), any(), eq(11L));
    }

    @Test
    void dryRunShouldNotCallReviewService() {
        DemoAccountCatalogService.DemoAccountDefinition general = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-01", "general01@demo.insidemovie.local", "일반01", "일반 계정 01", "GENERAL",
                EmotionType.JOY, 0.6f, 0.1f, 0.1f, 0.1f, 0.1f
        );

        DemoReviewSeedRow row = DemoReviewSeedRow.builder()
                .accountKey("general-01")
                .movieKoficId("20261150")
                .rating(4.5)
                .spoiler(false)
                .watchedAt(LocalDateTime.parse("2025-01-04T11:07:00"))
                .content("테스트 리뷰")
                .build();

        DemoDataBackfillProperties.Review reviewProperties = new DemoDataBackfillProperties.Review();
        reviewProperties.setFixturePath("seed/demo-reviews.v1.jsonl");
        reviewProperties.setTargetPerAccount(1);
        reviewProperties.setMaxContentLen(260);

        Member member = Member.builder().id(7L).email(general.email()).isBanned(false).build();
        Movie movie = Movie.builder().id(3L).koficId("20261150").title("점보").build();

        when(demoAccountCatalogService.getGeneralAccountDefinitions()).thenReturn(List.of(general));
        when(fixtureLoader.load("seed/demo-reviews.v1.jsonl", 260))
                .thenReturn(new DemoReviewFixtureLoadResult(List.of(row), 0));
        when(memberRepository.findByEmail(general.email())).thenReturn(Optional.of(member));
        when(movieRepository.findByKoficId("20261150")).thenReturn(Optional.of(movie));
        when(reviewRepository.findByMemberAndMovie(member, movie)).thenReturn(Optional.empty());

        ReviewAiSeedReport report = reviewAiSeedService.seed(true, reviewProperties);

        assertThat(report.getRequestedReviews()).isEqualTo(1);
        assertThat(report.getCreatedReviews()).isEqualTo(1);
        verify(reviewService, never()).createReview(eq(3L), any(), eq(7L));
    }
}
