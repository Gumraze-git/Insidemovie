package com.insidemovie.backend.api.review.controller;

import com.insidemovie.backend.api.constant.ReviewSort;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.review.dto.ReviewCreateDTO;
import com.insidemovie.backend.api.review.dto.ReviewCreatedResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.dto.ReviewUpdateDTO;
import com.insidemovie.backend.api.review.docs.ReviewApi;
import com.insidemovie.backend.api.review.service.ReviewService;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController implements ReviewApi {
    private final ReviewService reviewService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @PostMapping("/movies/{movieId}/reviews")
    public ResponseEntity<ReviewCreatedResponseDTO> createReview(
            @PathVariable Long movieId,
            @Valid @RequestBody ReviewCreateDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long reviewId = reviewService.createReview(movieId, request, currentUserIdResolver.resolve(jwt));
        URI location = ServletUriComponentsBuilder.fromPath("/api/v1/reviews/{id}")
                .buildAndExpand(reviewId)
                .toUri();
        return ResponseEntity.created(location)
                .body(ReviewCreatedResponseDTO.builder().reviewId(reviewId).build());
    }

    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<PageResDto<ReviewResponseDTO>> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "LATEST") ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, sort.toSort());
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId, pageable));
    }

    @GetMapping("/movies/{movieId}/reviews/mine")
    public ResponseEntity<ReviewResponseDTO> getMyReview(
            @PathVariable Long movieId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(reviewService.getMyReview(movieId, currentUserIdResolver.resolve(jwt)));
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateDTO request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        reviewService.modifyReview(reviewId, request, currentUserIdResolver.resolve(jwt));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        reviewService.deleteReview(reviewId, currentUserIdResolver.resolve(jwt));
        return ResponseEntity.noContent().build();
    }
}
