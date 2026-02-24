package com.insidemovie.backend.api.movie.bootstrap;

import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillReport;
import com.insidemovie.backend.api.movie.service.MovieMetadataBackfillService;
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
@ConditionalOnProperty(name = "movie.metadata.backfill.enabled", havingValue = "true")
@Slf4j
public class MovieMetadataBackfillRunner implements ApplicationRunner {

    private final MovieMetadataBackfillService movieMetadataBackfillService;
    private final ConfigurableApplicationContext context;

    @Value("${movie.metadata.backfill.dry-run:false}")
    private boolean dryRun;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            MovieMetadataBackfillReport report = movieMetadataBackfillService.backfill(dryRun);
            log.info("[MovieMetadataBackfill] completed dryRun={} requested={} succeeded={} failed={} ignored={} updatedPoster={} updatedBackdrop={} updatedOverview={}",
                    dryRun,
                    report.getRequestedMovies(),
                    report.getSucceededMovies(),
                    report.getFailedMovies(),
                    report.getIgnoredMovies(),
                    report.getUpdatedPosterCount(),
                    report.getUpdatedBackdropCount(),
                    report.getUpdatedOverviewCount());
        } catch (Exception e) {
            exitCode = 1;
            log.error("[MovieMetadataBackfill] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }
}
