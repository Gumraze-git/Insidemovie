package com.insidemovie.backend.api.movie.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieMetadataBackfillReport {
    private int requestedMovies;
    private int succeededMovies;
    private int failedMovies;
    private int ignoredMovies;
    private int updatedPosterCount;
    private int updatedBackdropCount;
    private int updatedOverviewCount;
}
