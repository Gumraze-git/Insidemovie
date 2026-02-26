package com.insidemovie.backend.api.movie.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "movie.metadata.poster.audit.enabled", havingValue = "true")
@Slf4j
public class MoviePosterAuditRunner implements ApplicationRunner {

    private final MoviePosterAuditService moviePosterAuditService;
    private final ConfigurableApplicationContext context;
    private final ObjectMapper objectMapper;

    @Value("${movie.metadata.poster.audit.dry-run:true}")
    private boolean dryRun;
    @Value("${movie.metadata.poster.audit.include-details:false}")
    private boolean includeDetails;
    @Value("${movie.metadata.poster.audit.report-path:build/reports/poster-audit.json}")
    private String reportPath;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            MoviePosterAuditReport report = moviePosterAuditService.auditAndBackfill(dryRun, includeDetails);
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
            writeReportFile(report);
        } catch (Exception e) {
            exitCode = 1;
            log.error("[MoviePosterAudit] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }

    private void writeReportFile(MoviePosterAuditReport report) {
        try {
            Path reportFile = Path.of(reportPath);
            if (reportFile.getParent() != null) {
                Files.createDirectories(reportFile.getParent());
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), report);
            log.info("[MoviePosterAudit] report written path={}", reportFile.toAbsolutePath());
        } catch (Exception e) {
            log.warn("[MoviePosterAudit] failed to write report file path={} reason={}", reportPath, e.getMessage());
        }
    }
}
