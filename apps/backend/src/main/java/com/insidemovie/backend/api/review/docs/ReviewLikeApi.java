package com.insidemovie.backend.api.review.docs;

import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Review Like", description = "Review like/unlike APIs")
@ApiCookieAuth
@ApiCommonErrorResponses
public interface ReviewLikeApi {

    @Operation(summary = "Like review")
    @ApiCreatedWithLocation
    @ApiNoContent
    ResponseEntity<Void> likeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(summary = "Unlike review")
    @ApiNoContent
    ResponseEntity<Void> unlikeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails
    );
}

