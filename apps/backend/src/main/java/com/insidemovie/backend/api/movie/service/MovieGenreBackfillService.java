package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.infrastructure.kobis.KobisMovieInfoClient;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieGenreBackfillService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final KobisMovieInfoClient kobisMovieInfoClient;
    private final GenreMappingService genreMappingService;

    @Transactional
    public MovieGenreBackfillReport backfill(boolean dryRun) {
        List<Movie> movies = movieRepository.findAllByKoficIdIsNotNull();
        long initialRows = movieGenreRepository.count();

        int requested = 0;
        int succeeded = 0;
        int failed = 0;
        int ignored = 0;
        int mappedRows = 0;

        for (Movie movie : movies) {
            requested++;
            try {
                String koficId = movie.getKoficId();
                if (koficId == null || koficId.isBlank()) {
                    ignored++;
                    continue;
                }

                var info = kobisMovieInfoClient.fetchMovieInfo(koficId);
                if (info.isEmpty()) {
                    failed++;
                    continue;
                }

                Set<GenreType> mapped = genreMappingService.mapGenres(info.get().genres());
                if (mapped.isEmpty()) {
                    ignored++;
                    continue;
                }

                mappedRows += mapped.size();

                if (!dryRun) {
                    movieGenreRepository.deleteByMovie(movie);
                    movieGenreRepository.saveAll(
                            mapped.stream()
                                    .map(genre -> MovieGenre.of(movie, genre))
                                    .toList()
                    );
                }

                succeeded++;
            } catch (Exception e) {
                failed++;
                log.warn("Movie genre backfill failed movieId={} koficId={} error={}",
                        movie.getId(), movie.getKoficId(), e.getMessage());
            }
        }

        long finalRows = dryRun ? initialRows : movieGenreRepository.count();
        return MovieGenreBackfillReport.builder()
                .requestedMovies(requested)
                .succeededMovies(succeeded)
                .failedMovies(failed)
                .ignoredMovies(ignored)
                .mappedGenreRows(mappedRows)
                .initialGenreRows(initialRows)
                .finalGenreRows(finalRows)
                .build();
    }
}
