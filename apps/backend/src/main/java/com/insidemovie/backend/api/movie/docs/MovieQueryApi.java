package com.insidemovie.backend.api.movie.docs;

import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import com.insidemovie.backend.api.movie.dto.tmdb.SearchMovieWrapperDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Movie Query", description = "Movie read-only query APIs")
@ApiCommonErrorResponses
public interface MovieQueryApi {

    @Operation(summary = "Get movie detail")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MovieDetailResDto> getMovieDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(summary = "Get movie emotions")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MovieEmotionResDTO> getMovieEmotions(@PathVariable Long movieId);

    @Operation(summary = "Get movie emotion summary")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<EmotionAvgDTO> getMovieEmotionSummary(@PathVariable Long movieId);

    @Operation(summary = "Search movies by title")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MovieSearchResDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam int page,
            @RequestParam int pageSize
    );

    @Operation(summary = "Search movies by query")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MovieSearchResDto>> searchByQuery(
            @RequestParam String q,
            @RequestParam int page,
            @RequestParam int pageSize
    );

    @Operation(summary = "Get popular movies")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<SearchMovieWrapperDTO> getPopularMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    );

    @Operation(summary = "Get recommended movies by latest")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MovieSearchResDto>> getRecommendedMoviesByLatest(
            @RequestParam GenreType genre,
            @RequestParam int page,
            @RequestParam int pageSize
    );

    @Operation(summary = "Get recommended movies by popularity")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MovieSearchResDto>> getRecommendedMoviesByPopularity(
            @RequestParam GenreType genre,
            @RequestParam int page,
            @RequestParam int pageSize
    );
}

