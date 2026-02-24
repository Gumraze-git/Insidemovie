package com.insidemovie.backend.api.movie.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieGenreBackfillReport {
    private final int requestedMovies;
    private final int succeededMovies;
    private final int failedMovies;
    private final int ignoredMovies;
    private final int mappedGenreRows;
    private final long initialGenreRows;
    private final long finalGenreRows;
}
