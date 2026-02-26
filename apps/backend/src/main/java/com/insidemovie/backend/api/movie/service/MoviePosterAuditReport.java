package com.insidemovie.backend.api.movie.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
    @Builder.Default
    private final List<MoviePosterAuditDetail> details = List.of();

    @Getter
    @Builder
    public static class MoviePosterAuditDetail {
        private final Long movieId;
        private final String koficId;
        private final String title;
        private final String reason;
    }
}
