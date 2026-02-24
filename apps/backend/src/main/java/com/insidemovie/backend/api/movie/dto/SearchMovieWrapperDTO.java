package com.insidemovie.backend.api.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchMovieWrapperDTO {

    private int page;
    private List<SearchMovieResponseDTO> results;
    private int totalPages;
    private int totalResults;
}
