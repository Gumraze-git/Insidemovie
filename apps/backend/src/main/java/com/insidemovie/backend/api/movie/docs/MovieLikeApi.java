package com.insidemovie.backend.api.movie.docs;

import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Movie Like", description = "Movie like/unlike APIs")
@ApiCookieAuth
@ApiCommonErrorResponses
public interface MovieLikeApi {

    @Operation(summary = "Like movie")
    @ApiCreatedWithLocation
    @ApiNoContent
    ResponseEntity<Void> likeMovie(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(summary = "Unlike movie")
    @ApiNoContent
    ResponseEntity<Void> unlikeMovie(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    );
}

