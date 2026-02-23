package com.insidemovie.backend.api.recommend.controller;

import com.insidemovie.backend.api.recommend.docs.MovieRecommendationApi;
import com.insidemovie.backend.api.recommend.dto.EmotionRequestDTO;
import com.insidemovie.backend.api.recommend.dto.MovieRecommendationDTO;
import com.insidemovie.backend.api.recommend.service.EmotionRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie-recommendations")
public class MovieRecommendationController implements MovieRecommendationApi {
    private final EmotionRecommendationService recommendationService;

    @PostMapping("/by-emotion")
    public ResponseEntity<List<MovieRecommendationDTO>> recommendMovies(@RequestBody EmotionRequestDTO request) {
        return ResponseEntity.ok(recommendationService.recommendByEmotion(request));
    }
}
