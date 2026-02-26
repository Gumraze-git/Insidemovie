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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieMetadataBackfillServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private KobisMovieInfoClient kobisMovieInfoClient;
    @Mock
    private KmdbMovieClient kmdbMovieClient;

    @InjectMocks
    private MovieMetadataBackfillService movieMetadataBackfillService;

    @Test
    void backfill_shouldUpdateMissingMetadataOnly() {
        Movie movie = Movie.builder()
                .id(1L)
                .koficId("20020234")
                .title("반지의 제왕 : 두 개의 탑")
                .releaseDate(LocalDate.of(2002, 12, 19))
                .overview("")
                .posterPath(null)
                .backdropPath(null)
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
                "http://backdrop.example/lotr2.jpg",
                "운명을 건 최후의 전쟁이 시작된다."
        );

        when(movieRepository.findAllByKoficIdIsNotNullAndMetadataMissing()).thenReturn(List.of(movie));
        when(kobisMovieInfoClient.fetchMovieInfo("20020234")).thenReturn(Optional.of(info));
        when(kmdbMovieClient.searchMovieCandidates("반지의 제왕 : 두 개의 탑", 2002, "피터 잭슨", 20))
                .thenReturn(List.of(candidate));

        MovieMetadataBackfillReport report = movieMetadataBackfillService.backfill(false);

        verify(movieRepository, times(1)).save(any(Movie.class));
        assertThat(report.getRequestedMovies()).isEqualTo(1);
        assertThat(report.getSucceededMovies()).isEqualTo(1);
        assertThat(report.getFailedMovies()).isEqualTo(0);
        assertThat(report.getIgnoredMovies()).isEqualTo(0);
        assertThat(report.getUpdatedPosterCount()).isEqualTo(1);
        assertThat(report.getUpdatedBackdropCount()).isEqualTo(1);
        assertThat(report.getUpdatedOverviewCount()).isEqualTo(1);
    }

    @Test
    void backfill_dryRunShouldNotPersist() {
        Movie movie = Movie.builder()
                .id(2L)
                .koficId("20241266")
                .title("대가족")
                .overview("")
                .posterPath(null)
                .backdropPath(null)
                .build();

        KobisMovieInfo info = new KobisMovieInfo(
                "20241266",
                "대가족",
                "",
                "20241211",
                2024,
                0,
                "대한민국",
                List.of("양우석"),
                List.of(),
                "12세관람가",
                List.of("드라마")
        );
        KmdbMovieCandidate candidate = new KmdbMovieCandidate(
                "대가족",
                "",
                2024,
                List.of("양우석"),
                "http://poster.example/family.jpg",
                "",
                "시놉시스"
        );

        when(movieRepository.findAllByKoficIdIsNotNullAndMetadataMissing()).thenReturn(List.of(movie));
        when(kobisMovieInfoClient.fetchMovieInfo("20241266")).thenReturn(Optional.of(info));
        when(kmdbMovieClient.searchMovieCandidates("대가족", 2024, "양우석", 20))
                .thenReturn(List.of(candidate));

        MovieMetadataBackfillReport report = movieMetadataBackfillService.backfill(true);

        verify(movieRepository, never()).save(any(Movie.class));
        assertThat(report.getSucceededMovies()).isEqualTo(1);
        assertThat(report.getUpdatedPosterCount()).isEqualTo(1);
        assertThat(report.getUpdatedOverviewCount()).isEqualTo(1);
    }

    @Test
    void backfill_shouldAcceptRelaxedMatchWhenScoreBelowDefaultThreshold() {
        Movie movie = Movie.builder()
                .id(3L)
                .koficId("20020234")
                .title("반지의 제왕 : 두 개의 탑")
                .releaseDate(LocalDate.of(2002, 12, 19))
                .overview("")
                .posterPath(null)
                .backdropPath(null)
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

        KmdbMovieCandidate relaxedCandidate = new KmdbMovieCandidate(
                "반지의 제왕",
                "The Lord of the Rings",
                2003,
                List.of("피터 잭슨"),
                "http://poster.example/lotr-relaxed.jpg",
                "",
                ""
        );

        when(movieRepository.findAllByKoficIdIsNotNullAndMetadataMissing()).thenReturn(List.of(movie));
        when(kobisMovieInfoClient.fetchMovieInfo("20020234")).thenReturn(Optional.of(info));
        when(kmdbMovieClient.searchMovieCandidates("반지의 제왕 : 두 개의 탑", 2002, "피터 잭슨", 20))
                .thenReturn(List.of(relaxedCandidate));

        MovieMetadataBackfillReport report = movieMetadataBackfillService.backfill(false);

        verify(movieRepository, times(1)).save(any(Movie.class));
        assertThat(report.getSucceededMovies()).isEqualTo(1);
        assertThat(report.getUpdatedPosterCount()).isEqualTo(1);
        assertThat(report.getIgnoredMovies()).isEqualTo(0);
    }
}
