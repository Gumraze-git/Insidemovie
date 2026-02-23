package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.MyMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.service.MovieLikeService;
import com.insidemovie.backend.api.movie.service.MovieService;
import com.insidemovie.backend.api.review.dto.ReviewResponseDTO;
import com.insidemovie.backend.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberActivityService {
    private final ReviewService reviewService;
    private final MovieLikeService movieLikeService;
    private final MovieService movieService;

    public PageResDto<ReviewResponseDTO> getMyReviews(String email, int page, int pageSize) {
        return reviewService.getMyReviews(email, page, pageSize);
    }

    public PageResDto<MyMovieResponseDTO> getMyLikedMovies(String email, int page, int pageSize) {
        return movieLikeService.getMyMovies(email, page, pageSize);
    }

    public PageResDto<MovieSearchResDto> getMyWatchedMovies(String email, int page, int pageSize) {
        return movieService.getMyWatchedMovies(email, page, pageSize);
    }
}
