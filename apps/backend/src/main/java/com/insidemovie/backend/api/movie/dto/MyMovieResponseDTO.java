package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMovieResponseDTO {
    private Long movieReactionId;
    private Long movieId;
    private String posterPath;
    private String title;
    private EmotionType mainEmotion;
    private Double mainEmotionValue;
    private BigDecimal ratingAvg;
}
