package com.insidemovie.backend.api.review.controller;

import com.insidemovie.backend.api.review.docs.ReviewLikeApi;
import com.insidemovie.backend.api.review.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
public class ReviewLikeController implements ReviewLikeApi {
    private final ReviewService reviewService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @PutMapping("/{reviewId}/likes/me")
    public ResponseEntity<Void> likeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        boolean created = reviewService.createReviewLike(reviewId, currentUserIdResolver.resolve(jwt));
        if (!created) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{reviewId}/likes/me")
    public ResponseEntity<Void> unlikeReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        reviewService.deleteReviewLike(reviewId, currentUserIdResolver.resolve(jwt));
        return ResponseEntity.noContent().build();
    }
}
