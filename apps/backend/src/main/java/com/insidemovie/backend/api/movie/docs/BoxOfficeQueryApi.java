package com.insidemovie.backend.api.movie.docs;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BoxOffice", description = "BoxOffice read-only APIs")
@ApiCommonErrorResponses
public interface BoxOfficeQueryApi {

    @Operation(summary = "Get daily boxoffice list")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>> getDaily(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    );

    @Operation(summary = "Get daily boxoffice movie detail")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MovieDetailResDto> getDailyMovieDetail(@PathVariable Long movieId);

    @Operation(summary = "Get weekly boxoffice list")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>> getWeekly(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "0") String weekGb,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    );

    @Operation(summary = "Get weekly boxoffice movie detail")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MovieDetailResDto> getWeeklyMovieDetail(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") String weekGb
    );
}

