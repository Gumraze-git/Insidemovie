package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.GenreType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GenreMappingServiceTest {

    private final GenreMappingService genreMappingService = new GenreMappingService();

    @Test
    void mapGenres_shouldMapAliasesAndDirectNames() {
        Set<GenreType> mapped = genreMappingService.mapGenres(
                List.of("멜로/로맨스", "서스펜스", "공상과학", "액션")
        );

        assertThat(mapped).contains(
                GenreType.로맨스,
                GenreType.스릴러,
                GenreType.SF,
                GenreType.액션
        );
    }

    @Test
    void mapGenres_shouldReturnEmptyWhenUnmapped() {
        Set<GenreType> mapped = genreMappingService.mapGenres(List.of("기타장르"));

        assertThat(mapped).isEmpty();
    }
}
