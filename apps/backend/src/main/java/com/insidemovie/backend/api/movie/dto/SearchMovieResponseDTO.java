package com.insidemovie.backend.api.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchMovieResponseDTO {
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backDropPath;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("popularity")
    private Double popularity;

    @JsonProperty("adult")
    private Boolean adult;
}
