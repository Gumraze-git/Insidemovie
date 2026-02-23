package com.insidemovie.backend.api.member.docs;

import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member Activity", description = "Current member activity APIs")
@ApiCookieAuth
@ApiCommonErrorResponses
public interface MemberActivityApi {

    @Operation(summary = "Get my reviews")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<ReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(summary = "Get my liked movies")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MyMovieResponseDTO>> getMyLikedMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(summary = "Get my watched movies")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<MovieSearchResDto>> getMyWatchedMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    );
}

