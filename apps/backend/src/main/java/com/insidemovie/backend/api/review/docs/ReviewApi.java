package com.insidemovie.backend.api.review.docs;

import com.insidemovie.backend.api.constant.ReviewSort;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.dto.ReviewCreatedResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewUpdateDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Review", description = "Review APIs")
@ApiCommonErrorResponses
public interface ReviewApi {

    @Operation(summary = "Create review")
    @ApiCookieAuth
    @ApiCreatedWithLocation
    ResponseEntity<ReviewCreatedResponseDTO> createReview(
            @PathVariable Long movieId,
            @Valid @RequestBody ReviewCreateDTO request,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(summary = "Get reviews by movie")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResDto<ReviewResponseDTO>> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "LATEST") ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get my review for movie")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<ReviewResponseDTO> getMyReview(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(summary = "Update review")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateDTO request,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(summary = "Delete review")
    @ApiCookieAuth
    @ApiNoContent
    ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt
    );
}

