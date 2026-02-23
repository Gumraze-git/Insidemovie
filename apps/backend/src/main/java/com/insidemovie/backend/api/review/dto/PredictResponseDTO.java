package com.insidemovie.backend.api.review.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@NoArgsConstructor
public class PredictResponseDTO {
    private String text;
    private String aggregation;
    private Map<String, Double> probabilities;

    @JsonAlias({"instant", "analyzedAt"})
    private Instant analyzedAt;
}
