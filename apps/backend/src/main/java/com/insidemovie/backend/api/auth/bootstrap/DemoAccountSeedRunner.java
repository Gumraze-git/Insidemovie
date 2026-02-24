package com.insidemovie.backend.api.auth.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountSeedReport;
import com.insidemovie.backend.api.auth.service.DemoAccountSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.accounts.seed-enabled", havingValue = "true")
public class DemoAccountSeedRunner implements CommandLineRunner {

    private final DemoAccountSeedService demoAccountSeedService;

    @Override
    public void run(String... args) {
        DemoAccountSeedReport report = demoAccountSeedService.seed(false);
        log.info(
                "Demo account seeding completed - total={}, createdMembers={}, updatedMembers={}, createdEmotionSummaries={}, updatedEmotionSummaries={}",
                report.getTotalDefinitions(),
                report.getCreatedMembers(),
                report.getUpdatedMembers(),
                report.getCreatedEmotionSummaries(),
                report.getUpdatedEmotionSummaries()
        );
    }
}
