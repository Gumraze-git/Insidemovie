package com.insidemovie.backend.api.movie.bootstrap;

import com.insidemovie.backend.api.movie.service.MovieGenreBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieGenreBackfillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "movie.genre.backfill.enabled", havingValue = "true")
@Slf4j
public class MovieGenreBackfillRunner implements ApplicationRunner {

    private final MovieGenreBackfillService movieGenreBackfillService;
    private final ConfigurableApplicationContext context;

    @Value("${movie.genre.backfill.dry-run:false}")
    private boolean dryRun;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            MovieGenreBackfillReport report = movieGenreBackfillService.backfill(dryRun);
            log.info("[MovieGenreBackfill] completed dryRun={} requested={} succeeded={} failed={} ignored={} mappedRows={} initialRows={} finalRows={}",
                    dryRun,
                    report.getRequestedMovies(),
                    report.getSucceededMovies(),
                    report.getFailedMovies(),
                    report.getIgnoredMovies(),
                    report.getMappedGenreRows(),
                    report.getInitialGenreRows(),
                    report.getFinalGenreRows());
        } catch (Exception e) {
            exitCode = 1;
            log.error("[MovieGenreBackfill] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }
}
