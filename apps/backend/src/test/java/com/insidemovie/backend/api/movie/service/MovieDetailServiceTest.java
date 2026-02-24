package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieLikeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieDetailServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieGenreRepository movieGenreRepository;
    @Mock
    private MovieLikeRepository movieLikeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @Test
    void getMovieDetail_shouldUseDefaultOverviewWhenBlank() {
        MovieDetailService movieDetailService = new MovieDetailService(
                new ObjectMapper(),
                movieRepository,
                movieGenreRepository,
                movieLikeRepository,
                memberRepository,
                reviewRepository
        );

        Movie movie = Movie.builder()
                .id(1L)
                .title("테스트 영화")
                .titleEn("Test Movie")
                .overview(" ")
                .posterPath(null)
                .backdropPath(null)
                .originalLanguage("한국")
                .actors("[]")
                .directors("[]")
                .ottProviders("[]")
                .rating("12세관람가")
                .runtime(120)
                .status("RELEASED")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieGenreRepository.findByMovieId(1L)).thenReturn(List.of());
        when(reviewRepository.findAverageByMovieId(1L)).thenReturn(null);

        MovieDetailResDto response = movieDetailService.getMovieDetail(1L);

        assertThat(response.getOverview()).isEqualTo("시놉시스 준비 중입니다.");
        assertThat(response.getPosterPath()).isNull();
    }
}
