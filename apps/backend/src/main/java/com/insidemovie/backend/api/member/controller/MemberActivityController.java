package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.member.docs.MemberActivityApi;
import com.insidemovie.backend.api.member.service.MemberActivityService;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class MemberActivityController implements MemberActivityApi {
    private final MemberActivityService memberActivityService;

    @GetMapping("/reviews")
    public ResponseEntity<PageResDto<ReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(memberActivityService.getMyReviews(userDetails.getUsername(), page, pageSize));
    }

    @GetMapping("/liked-movies")
    public ResponseEntity<PageResDto<MyMovieResponseDTO>> getMyLikedMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(memberActivityService.getMyLikedMovies(userDetails.getUsername(), page, pageSize));
    }

    @GetMapping("/watched-movies")
    public ResponseEntity<PageResDto<MovieSearchResDto>> getMyWatchedMovies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ResponseEntity.ok(memberActivityService.getMyWatchedMovies(userDetails.getUsername(), page, pageSize));
    }
}
