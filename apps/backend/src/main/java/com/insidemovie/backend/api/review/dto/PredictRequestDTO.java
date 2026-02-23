package com.insidemovie.backend.api.review.dto;

import lombok.Getter;

@Getter
public class PredictRequestDTO {
    private final String text;
    private final String aggregation;

    public PredictRequestDTO(String text) {
        this(text, "overall_avg");
    }

    public PredictRequestDTO(String text, String aggregation) {
        this.text = text;
        this.aggregation = aggregation;
    }
}
