package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceRecommendationFallbackTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieGenreRepository movieGenreRepository;
    @Mock
    private EmotionRepository emotionRepository;
    @Mock
    private MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MovieGenreBackfillService movieGenreBackfillService;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id(1L)
                .title("테스트 영화")
                .posterPath("poster.jpg")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .build();
    }

    @Test
    void recommendByPopularity_shouldFallbackWhenGenreTableEmpty() {
        Page<Movie> fallbackPage = new PageImpl<>(List.of(movie), PageRequest.of(0, 20), 1);
        stubMovieSummary();

        when(movieGenreRepository.count()).thenReturn(0L, 0L);
        when(movieRepository.findAllByOrderByPopularityDesc(any(Pageable.class))).thenReturn(fallbackPage);
        when(movieGenreBackfillService.backfill(false)).thenReturn(
                MovieGenreBackfillReport.builder()
                        .requestedMovies(1)
                        .succeededMovies(0)
                        .failedMovies(1)
                        .ignoredMovies(0)
                        .mappedGenreRows(0)
                        .initialGenreRows(0)
                        .finalGenreRows(0)
                        .build()
        );

        PageResDto<MovieSearchResDto> response = movieService.getRecommendedMoviesByPopularity(GenreType.액션, 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        verify(movieRepository, never()).findMoviesByGenreTypeOrderByPopularityDesc(eq(GenreType.액션), any(Pageable.class));
    }

    @Test
    void recommendByPopularity_shouldKeepGenreFilterWhenGenreTableExists() {
        when(movieGenreRepository.count()).thenReturn(2L);
        when(movieRepository.findMoviesByGenreTypeOrderByPopularityDesc(eq(GenreType.액션), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatThrownBy(() -> movieService.getRecommendedMoviesByPopularity(GenreType.액션, 0, 20))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("해당 장르의 영화가 없습니다");

        verify(movieRepository, never()).findAllByOrderByPopularityDesc(any(Pageable.class));
    }

    private void stubMovieSummary() {
        EmotionAvgDTO avg = EmotionAvgDTO.builder()
                .joy(0.7)
                .sadness(0.1)
                .anger(0.1)
                .fear(0.05)
                .disgust(0.05)
                .repEmotionType(EmotionType.JOY)
                .build();
        MovieEmotionSummary summary = MovieEmotionSummary.builder()
                .movie(movie)
                .dominantEmotion(EmotionType.JOY)
                .joy(0.7f)
                .sadness(0.1f)
                .anger(0.1f)
                .fear(0.05f)
                .disgust(0.05f)
                .build();
        when(emotionRepository.findAverageEmotionsByMovieId(1L)).thenReturn(Optional.of(avg));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieEmotionSummaryRepository.findByMovieId(1L)).thenReturn(Optional.of(summary));
        when(reviewRepository.findAverageByMovieId(1L)).thenReturn(4.0);
    }
}
