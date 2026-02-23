package com.insidemovie.backend.api.match.controller;

import com.insidemovie.backend.api.match.dto.VoteCreateRequest;
import com.insidemovie.backend.api.match.dto.WinnerHistoryDto;
import com.insidemovie.backend.api.match.docs.MatchQueryApi;
import com.insidemovie.backend.api.match.service.MatchService;
import com.insidemovie.backend.api.movie.dto.MovieDetailSimpleResDto;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchQueryController implements MatchQueryApi {
    private final MatchService matchService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @GetMapping("/current")
    public ResponseEntity<List<MovieDetailSimpleResDto>> getCurrentMatch() {
        return ResponseEntity.ok(matchService.getMatchDetail());
    }

    @GetMapping("/winners")
    public ResponseEntity<List<WinnerHistoryDto>> getWinners() {
        return ResponseEntity.ok(matchService.getWinnerHistory());
    }

    @PostMapping("/current/votes")
    public ResponseEntity<Map<String, Long>> voteCurrentMatch(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VoteCreateRequest request
    ) {
        Long voteId = matchService.voteMatch(request.getMovieId(), currentUserIdResolver.resolve(jwt));
        URI location = ServletUriComponentsBuilder.fromPath("/api/v1/matches/current/votes/{id}")
                .buildAndExpand(voteId)
                .toUri();
        return ResponseEntity.created(location).body(Map.of("voteId", voteId));
    }
}
