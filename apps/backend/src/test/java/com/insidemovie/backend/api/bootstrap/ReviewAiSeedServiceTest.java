package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.KmdbMovieClient;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.api.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private KobisMovieInfoClient kobisMovieInfoClient;
    @Mock
    private KmdbMovieClient kmdbMovieClient;
    @Mock
    private WebSynopsisClient webSynopsisClient;

    @InjectMocks
    private ReviewAiSeedService reviewAiSeedService;

    @Test
    void shouldCreateOnlyMissingReviewsPerGeneralAccount() {
        DemoAccountCatalogService.DemoAccountDefinition general = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-01", "general01@demo.insidemovie.local", "일반01", "일반 계정 01", "GENERAL",
                EmotionType.JOY, 0.6f, 0.1f, 0.1f, 0.1f, 0.1f
        );

        Member member = Member.builder().id(11L).email(general.email()).isBanned(false).build();
        Movie movie = Movie.builder()
                .id(1L)
                .title("테스트 영화")
                .overview("주인공이 성장하는 이야기입니다.")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .build();

        when(demoAccountCatalogService.getGeneralAccountDefinitions()).thenReturn(List.of(general));
        when(memberRepository.findByEmail(general.email())).thenReturn(Optional.of(member));
        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(reviewRepository.countByMember(member)).thenReturn(19L);
        when(reviewRepository.findMovieIdsByMemberId(11L)).thenReturn(List.of());
        when(reviewService.createReview(eq(1L), any(), eq(11L))).thenReturn(1L);

        ReviewAiSeedReport report = reviewAiSeedService.seed(false, 20, false);

        assertThat(report.getRequestedReviews()).isEqualTo(1);
        assertThat(report.getCreatedReviews()).isEqualTo(1);
        assertThat(report.getFailedReviews()).isZero();
        verify(reviewService, times(1)).createReview(eq(1L), any(), eq(11L));
    }

    @Test
    void dryRunShouldNotCallReviewService() {
        DemoAccountCatalogService.DemoAccountDefinition general = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-01", "general01@demo.insidemovie.local", "일반01", "일반 계정 01", "GENERAL",
                EmotionType.JOY, 0.6f, 0.1f, 0.1f, 0.1f, 0.1f
        );

        Member member = Member.builder().id(7L).email(general.email()).isBanned(false).build();
        Movie movie = Movie.builder().id(3L).title("테스트 영화2").overview("설명이 있는 영화").build();

        when(demoAccountCatalogService.getGeneralAccountDefinitions()).thenReturn(List.of(general));
        when(memberRepository.findByEmail(general.email())).thenReturn(Optional.of(member));
        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(reviewRepository.countByMember(member)).thenReturn(0L);
        when(reviewRepository.findMovieIdsByMemberId(7L)).thenReturn(List.of());

        ReviewAiSeedReport report = reviewAiSeedService.seed(true, 1, true);

        assertThat(report.getRequestedReviews()).isEqualTo(1);
        assertThat(report.getCreatedReviews()).isEqualTo(1);
        verify(reviewService, never()).createReview(eq(3L), any(), eq(7L));
    }
}

