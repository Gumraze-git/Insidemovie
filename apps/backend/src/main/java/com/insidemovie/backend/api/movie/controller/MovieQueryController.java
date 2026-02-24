package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.dto.SearchMovieWrapperDTO;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import com.insidemovie.backend.api.movie.docs.MovieQueryApi;
import com.insidemovie.backend.api.movie.service.MovieDetailService;
import com.insidemovie.backend.api.movie.service.MovieService;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movies")
public class MovieQueryController implements MovieQueryApi {
    private final MovieService movieService;
    private final MovieDetailService movieDetailService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResDto> getMovieDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        MovieDetailResDto dto = jwt == null
                ? movieDetailService.getMovieDetail(id)
                : movieDetailService.getMovieDetail(id, currentUserIdResolver.resolve(jwt));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{movieId}/emotions")
    public ResponseEntity<MovieEmotionResDTO> getMovieEmotions(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieService.getMovieEmotions(movieId));
    }

    @GetMapping("/{movieId}/emotion-summary")
    public ResponseEntity<EmotionAvgDTO> getMovieEmotionSummary(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieService.getMovieEmotionSummary(movieId));
    }

    @GetMapping("/search/title")
    public ResponseEntity<PageResDto<MovieSearchResDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam int page,
            @RequestParam int pageSize
    ) {
        return ResponseEntity.ok(movieService.movieSearchTitle(title, page, pageSize));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResDto<MovieSearchResDto>> searchByQuery(
            @RequestParam String q,
            @RequestParam int page,
            @RequestParam int pageSize
    ) {
        return ResponseEntity.ok(movieService.searchByQuery(q, page, pageSize));
    }

    @GetMapping("/popular")
    public ResponseEntity<SearchMovieWrapperDTO> getPopularMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(movieService.getPopularMovies(page, pageSize));
    }

    @GetMapping("/recommend/latest")
    public ResponseEntity<PageResDto<MovieSearchResDto>> getRecommendedMoviesByLatest(
            @RequestParam GenreType genre,
            @RequestParam int page,
            @RequestParam int pageSize
    ) {
        return ResponseEntity.ok(movieService.getRecommendedMoviesByLatest(genre, page, pageSize));
    }

    @GetMapping("/recommend/popular")
    public ResponseEntity<PageResDto<MovieSearchResDto>> getRecommendedMoviesByPopularity(
            @RequestParam GenreType genre,
            @RequestParam int page,
            @RequestParam int pageSize
    ) {
        return ResponseEntity.ok(movieService.getRecommendedMoviesByPopularity(genre, page, pageSize));
    }
}
