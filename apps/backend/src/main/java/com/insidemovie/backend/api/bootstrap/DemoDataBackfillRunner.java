package com.insidemovie.backend.api.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.data.backfill.enabled", havingValue = "true")
@Slf4j
public class DemoDataBackfillRunner implements ApplicationRunner {

    private final DemoDataBackfillProperties properties;
    private final DemoDataBackfillService demoDataBackfillService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            boolean dryRun = properties.isDryRun();
            DemoDataBackfillReport report = demoDataBackfillService.run(dryRun);
            log.info("[DemoDataBackfill] completed dryRun={} accountsCreated={} accountsUpdated={} genreMapped={} metadataUpdatedPoster={} metadataUpdatedOverview={} metadataUpdatedBackdrop={} reviewsRequested={} reviewsCreated={} reviewsSkipped={} reviewsFailed={} reviewFixtureLoaded={} reviewFixtureInvalid={} emotionsCreated={} matchesClosedCreated={} currentCreated={} votesCreated={}",
                    dryRun,
                    report.getAccountsCreated(),
                    report.getAccountsUpdated(),
                    report.getGenreMapped(),
                    report.getMetadataUpdatedPoster(),
                    report.getMetadataUpdatedOverview(),
                    report.getMetadataUpdatedBackdrop(),
                    report.getReviewsRequested(),
                    report.getReviewsCreated(),
                    report.getReviewsSkipped(),
                    report.getReviewsFailed(),
                    report.getReviewFixtureLoaded(),
                    report.getReviewFixtureInvalid(),
                    report.getEmotionsCreated(),
                    report.getMatchesClosedCreated(),
                    report.getCurrentCreated(),
                    report.getVotesCreated()
            );
        } catch (Exception e) {
            exitCode = 1;
            log.error("[DemoDataBackfill] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }
}
