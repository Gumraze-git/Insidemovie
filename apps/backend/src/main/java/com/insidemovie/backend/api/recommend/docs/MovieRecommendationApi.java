package com.insidemovie.backend.api.recommend.docs;

import com.insidemovie.backend.api.recommend.dto.EmotionRequestDTO;
import com.insidemovie.backend.api.recommend.dto.MovieRecommendationDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Recommendation", description = "Movie recommendation APIs")
@ApiCommonErrorResponses
public interface MovieRecommendationApi {

    @Operation(summary = "Recommend movies by emotion")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<List<MovieRecommendationDTO>> recommendMovies(@RequestBody EmotionRequestDTO request);
}

