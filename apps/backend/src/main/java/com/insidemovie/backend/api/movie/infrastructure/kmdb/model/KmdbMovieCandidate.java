package com.insidemovie.backend.api.movie.infrastructure.kmdb.model;

import java.util.List;

public record KmdbMovieCandidate(
        String title,
        String titleEn,
        Integer productionYear,
        List<String> directors,
        String posterPath,
        String backdropPath,
        String overview
) {
    public boolean hasMetadata() {
        return (posterPath != null && !posterPath.isBlank())
                || (backdropPath != null && !backdropPath.isBlank())
                || (overview != null && !overview.isBlank());
    }
}
