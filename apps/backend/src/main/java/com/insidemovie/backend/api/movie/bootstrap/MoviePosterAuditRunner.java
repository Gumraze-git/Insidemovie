package com.insidemovie.backend.api.movie.bootstrap;

import com.insidemovie.backend.api.movie.service.MoviePosterAuditReport;
import com.insidemovie.backend.api.movie.service.MoviePosterAuditService;
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
@ConditionalOnProperty(name = "movie.metadata.poster.audit.enabled", havingValue = "true")
@Slf4j
public class MoviePosterAuditRunner implements ApplicationRunner {

    private final MoviePosterAuditService moviePosterAuditService;
    private final ConfigurableApplicationContext context;

    @Value("${movie.metadata.poster.audit.dry-run:true}")
    private boolean dryRun;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            MoviePosterAuditReport report = moviePosterAuditService.auditAndBackfill(dryRun);
            log.info("[MoviePosterAudit] completed dryRun={} totalMovies={} targetMissingPosterMovies={} alreadyHasPoster={} kobisNoPosterSource={} kmdbNoResult={} kmdbResultNoPoster={} matchScoreBelowThreshold={} matchedUpdated={} failed={}",
                    dryRun,
                    report.getTotalMovies(),
                    report.getTargetMissingPosterMovies(),
                    report.getAlreadyHasPoster(),
                    report.getKobisNoPosterSource(),
                    report.getKmdbNoResult(),
                    report.getKmdbResultNoPoster(),
                    report.getMatchScoreBelowThreshold(),
                    report.getMatchedUpdated(),
                    report.getFailed());
        } catch (Exception e) {
            exitCode = 1;
            log.error("[MoviePosterAudit] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }
}
