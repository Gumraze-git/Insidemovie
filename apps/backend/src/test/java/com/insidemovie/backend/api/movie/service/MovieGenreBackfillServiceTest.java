package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.infrastructure.kobis.model.KobisMovieInfo;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieGenreBackfillServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieGenreRepository movieGenreRepository;
    @Mock
    private KobisMovieInfoClient kobisMovieInfoClient;
    @Mock
    private GenreMappingService genreMappingService;

    @InjectMocks
    private MovieGenreBackfillService movieGenreBackfillService;

    @Test
    void backfill_shouldDeleteAndInsertGenresIdempotently() {
        Movie movie = Movie.builder()
                .id(1L)
                .koficId("20240001")
                .build();

        KobisMovieInfo info = new KobisMovieInfo(
                "20240001", "title", "titleEn", "20240101",
                2024, 120, "대한민국", List.of(), List.of(), "12세", List.of("멜로/로맨스")
        );

        when(movieRepository.findAllByKoficIdIsNotNull()).thenReturn(List.of(movie));
        when(movieGenreRepository.count()).thenReturn(0L, 1L);
        when(kobisMovieInfoClient.fetchMovieInfo("20240001")).thenReturn(Optional.of(info));
        when(genreMappingService.mapGenres(info.genres())).thenReturn(Set.of(GenreType.로맨스));

        MovieGenreBackfillReport report = movieGenreBackfillService.backfill(false);

        verify(movieGenreRepository, times(1)).deleteByMovie(movie);
        ArgumentCaptor<List<MovieGenre>> genresCaptor = ArgumentCaptor.forClass(List.class);
        verify(movieGenreRepository, times(1)).saveAll(genresCaptor.capture());
        assertThat(genresCaptor.getValue()).hasSize(1);
        assertThat(genresCaptor.getValue().get(0).getGenreType()).isEqualTo(GenreType.로맨스);

        assertThat(report.getRequestedMovies()).isEqualTo(1);
        assertThat(report.getSucceededMovies()).isEqualTo(1);
        assertThat(report.getFailedMovies()).isEqualTo(0);
        assertThat(report.getIgnoredMovies()).isEqualTo(0);
        assertThat(report.getMappedGenreRows()).isEqualTo(1);
        assertThat(report.getInitialGenreRows()).isEqualTo(0L);
        assertThat(report.getFinalGenreRows()).isEqualTo(1L);
    }

    @Test
    void backfill_dryRunShouldNotMutate() {
        Movie movie = Movie.builder().id(1L).koficId("20240001").build();
        KobisMovieInfo info = new KobisMovieInfo(
                "20240001", "title", "titleEn", "20240101",
                2024, 120, "대한민국", List.of(), List.of(), "12세", List.of("액션")
        );

        when(movieRepository.findAllByKoficIdIsNotNull()).thenReturn(List.of(movie));
        when(movieGenreRepository.count()).thenReturn(5L);
        when(kobisMovieInfoClient.fetchMovieInfo("20240001")).thenReturn(Optional.of(info));
        when(genreMappingService.mapGenres(eq(info.genres()))).thenReturn(Set.of(GenreType.액션));

        MovieGenreBackfillReport report = movieGenreBackfillService.backfill(true);

        verify(movieGenreRepository, never()).deleteByMovie(any(Movie.class));
        verify(movieGenreRepository, never()).saveAll(any());
        assertThat(report.getInitialGenreRows()).isEqualTo(5L);
        assertThat(report.getFinalGenreRows()).isEqualTo(5L);
    }
}
