package com.insidemovie.backend.api.match.docs;

import com.insidemovie.backend.api.match.dto.VoteCreateRequest;
import com.insidemovie.backend.api.match.dto.WinnerHistoryDto;
import com.insidemovie.backend.api.movie.dto.MovieDetailSimpleResDto;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "Match", description = "Match APIs")
@ApiCommonErrorResponses
public interface MatchQueryApi {

    @Operation(summary = "Get current match")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<List<MovieDetailSimpleResDto>> getCurrentMatch();

    @Operation(summary = "Get match winners")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<List<WinnerHistoryDto>> getWinners();

    @Operation(summary = "Vote current match")
    @ApiCookieAuth
    @ApiCreatedWithLocation
    ResponseEntity<Map<String, Long>> voteCurrentMatch(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VoteCreateRequest request
    );
}

