package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/boxoffice")
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeQueryController {
    private final BoxOfficeService boxOfficeService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @GetMapping("/daily")
    public ResponseEntity<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>> getDaily(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        String resolved = (targetDt == null || targetDt.isBlank())
                ? LocalDate.now().minusDays(1).format(FMT)
                : targetDt;
        log.info("[Controller] resolved daily targetDt={}", resolved);

        BoxOfficeListDTO<DailyBoxOfficeResponseDTO> dto =
                boxOfficeService.getSavedDailyBoxOffice(resolved, itemPerPage);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/daily/movies/{movieId}")
    public ResponseEntity<MovieDetailResDto> getDailyMovieDetail(@PathVariable Long movieId) {
        return ResponseEntity.ok(boxOfficeService.getDailyMovieDetailByMovieId(movieId));
    }

    @GetMapping("/weekly")
    public ResponseEntity<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>> getWeekly(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "0") String weekGb,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        return ResponseEntity.ok(boxOfficeService.getSavedWeeklyBoxOffice(targetDt, weekGb, itemPerPage));
    }

    @GetMapping("/weekly/movies/{movieId}")
    public ResponseEntity<MovieDetailResDto> getWeeklyMovieDetail(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") String weekGb
    ) {
        return ResponseEntity.ok(boxOfficeService.getWeeklyMovieDetailByMovieId(movieId, weekGb));
    }
}
