package com.insidemovie.backend.api.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieRecommendationApiResponseDto {
    private Integer count;
    private List<MovieSimilarityResDto> items;
}
