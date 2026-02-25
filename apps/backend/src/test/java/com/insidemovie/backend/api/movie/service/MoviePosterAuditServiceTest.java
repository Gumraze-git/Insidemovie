package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.KmdbMovieClient;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.model.KmdbMovieCandidate;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.infrastructure.kobis.model.KobisMovieInfo;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoviePosterAuditServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private KobisMovieInfoClient kobisMovieInfoClient;
    @Mock
    private KmdbMovieClient kmdbMovieClient;

    @InjectMocks
    private MoviePosterAuditService moviePosterAuditService;

    @Test
    void auditShouldClassifyMatchedUpdated() {
        Movie movie = Movie.builder()
                .id(1L)
                .koficId("20020234")
                .title("반지의 제왕 : 두 개의 탑")
                .releaseDate(LocalDate.of(2002, 12, 19))
                .posterPath(null)
                .build();

        KobisMovieInfo info = new KobisMovieInfo(
                "20020234",
                "반지의 제왕 : 두 개의 탑",
                "The Lord of the Rings: The Two Towers",
                "20021219",
                2002,
                179,
                "미국",
                List.of("피터 잭슨"),
                List.of(),
                "12세관람가",
                List.of("액션", "판타지")
        );

        KmdbMovieCandidate candidate = new KmdbMovieCandidate(
                "반지의 제왕 : 두 개의 탑",
                "The Lord of the Rings: The Two Towers",
                2002,
                List.of("피터 잭슨"),
                "http://poster.example/lotr2.jpg",
                "",
                "운명을 건 최후의 전쟁이 시작된다."
        );

        when(movieRepository.findAllByKoficIdIsNotNull()).thenReturn(List.of(movie));
        when(kobisMovieInfoClient.fetchMovieInfo("20020234")).thenReturn(Optional.of(info));
        when(kmdbMovieClient.searchMovieCandidates(anyString(), nullable(Integer.class), anyString(), anyInt()))
                .thenReturn(List.of(candidate));

        MoviePosterAuditReport report = moviePosterAuditService.auditAndBackfill(true);

        assertThat(report.getTotalMovies()).isEqualTo(1);
        assertThat(report.getTargetMissingPosterMovies()).isEqualTo(1);
        assertThat(report.getKobisNoPosterSource()).isEqualTo(1);
        assertThat(report.getMatchedUpdated()).isEqualTo(1);
        assertThat(report.getKmdbNoResult()).isZero();
        assertThat(report.getKmdbResultNoPoster()).isZero();
        assertThat(report.getMatchScoreBelowThreshold()).isZero();
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void auditShouldClassifyKmdbNoResult() {
        Movie movie = Movie.builder()
                .id(2L)
                .koficId("20250000")
                .title("매칭 실패 영화")
                .posterPath(null)
                .build();

        when(movieRepository.findAllByKoficIdIsNotNull()).thenReturn(List.of(movie));
        when(kobisMovieInfoClient.fetchMovieInfo("20250000")).thenReturn(Optional.empty());
        when(kmdbMovieClient.searchMovieCandidates(anyString(), nullable(Integer.class), anyString(), anyInt()))
                .thenReturn(List.of());

        MoviePosterAuditReport report = moviePosterAuditService.auditAndBackfill(true);

        assertThat(report.getTargetMissingPosterMovies()).isEqualTo(1);
        assertThat(report.getKobisNoPosterSource()).isEqualTo(1);
        assertThat(report.getKmdbNoResult()).isEqualTo(1);
        assertThat(report.getMatchedUpdated()).isZero();
    }
}
