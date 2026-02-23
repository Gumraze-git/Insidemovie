package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.docs.MovieLikeApi;
import com.insidemovie.backend.api.movie.service.MovieLikeService;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movies")
public class MovieLikeController implements MovieLikeApi {
    private final MovieLikeService movieLikeService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @PutMapping("/{movieId}/likes/me")
    public ResponseEntity<Void> likeMovie(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        boolean created = movieLikeService.createMovieLike(movieId, currentUserIdResolver.resolve(jwt));
        if (!created) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{movieId}/likes/me")
    public ResponseEntity<Void> unlikeMovie(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        movieLikeService.deleteMovieLike(movieId, currentUserIdResolver.resolve(jwt));
        return ResponseEntity.noContent().build();
    }
}
