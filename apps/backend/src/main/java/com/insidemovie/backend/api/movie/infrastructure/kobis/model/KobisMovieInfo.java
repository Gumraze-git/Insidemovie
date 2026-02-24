package com.insidemovie.backend.api.movie.infrastructure.kobis.model;

import java.util.List;

public record KobisMovieInfo(
        String movieCd,
        String title,
        String titleEn,
        String openDt,
        Integer productionYear,
        Integer runtime,
        String nation,
        List<String> directors,
        List<String> actors,
        String rating,
        List<String> genres
) {
}
