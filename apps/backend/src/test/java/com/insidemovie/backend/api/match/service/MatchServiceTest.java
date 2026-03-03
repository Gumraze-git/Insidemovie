package com.insidemovie.backend.api.match.service;

import com.insidemovie.backend.api.match.dto.WinnerHistoryDto;
import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.match.repository.VoteRepository;
import com.insidemovie.backend.api.member.service.MemberPolicyService;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MovieMatchRepository movieMatchRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private MemberPolicyService memberPolicyService;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void getWinnerHistoryShouldSkipOrphanWinnerReferences() {
        Match orphanWinnerMatch = Match.builder()
                .id(100L)
                .matchNumber(9)
                .matchDate(LocalDate.of(2026, 3, 1))
                .winnerId(200L)
                .build();

        Match validWinnerMatch = Match.builder()
                .id(101L)
                .matchNumber(8)
                .matchDate(LocalDate.of(2026, 2, 22))
                .winnerId(201L)
                .build();

        Movie winnerMovie = Movie.builder()
                .id(201L)
                .title("테스트 우승 영화")
                .posterPath("https://example.com/poster.jpg")
                .build();

        when(matchRepository.findAllByWinnerIdIsNotNullOrderByMatchNumberDesc())
                .thenReturn(List.of(orphanWinnerMatch, validWinnerMatch));
        when(movieRepository.findById(200L)).thenReturn(Optional.empty());
        when(movieRepository.findById(201L)).thenReturn(Optional.of(winnerMovie));
        when(reviewRepository.findAverageByMovieId(201L)).thenReturn(4.256d);

        List<WinnerHistoryDto> result = matchService.getWinnerHistory();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMatchNumber()).isEqualTo(8);
        assertThat(result.get(0).getMovie().getId()).isEqualTo(201L);
        assertThat(result.get(0).getMovie().getRatingAvg()).isEqualByComparingTo("4.26");
    }
}
