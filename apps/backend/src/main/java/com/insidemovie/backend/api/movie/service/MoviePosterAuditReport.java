package com.insidemovie.backend.api.movie.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MoviePosterAuditReport {
    private final int totalMovies;
    private final int targetMissingPosterMovies;
    private final int alreadyHasPoster;
    private final int kobisNoPosterSource;
    private final int kmdbNoResult;
    private final int kmdbResultNoPoster;
    private final int matchScoreBelowThreshold;
    private final int matchedUpdated;
    private final int failed;
}
