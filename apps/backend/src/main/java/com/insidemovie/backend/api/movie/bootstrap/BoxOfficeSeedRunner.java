package com.insidemovie.backend.api.movie.bootstrap;

import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
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
@ConditionalOnProperty(name = "movie.boxoffice.seed.enabled", havingValue = "true")
@Slf4j
public class BoxOfficeSeedRunner implements ApplicationRunner {

    @Value("${movie.boxoffice.seed.dry-run:false}")
    private boolean dryRun;

    @Value("${movie.boxoffice.seed.include-daily:true}")
    private boolean includeDaily;

    @Value("${movie.boxoffice.seed.include-weekly:true}")
    private boolean includeWeekly;

    @Value("${movie.boxoffice.seed.item-per-page:10}")
    private int itemPerPage;

    @Value("${movie.boxoffice.seed.week-gb:0}")
    private String weekGb;

    private final BoxOfficeService boxOfficeService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = 0;
        try {
            if (dryRun) {
                log.info("[BoxOfficeSeed] dryRun=true includeDaily={} includeWeekly={} itemPerPage={} weekGb={}",
                        includeDaily, includeWeekly, itemPerPage, weekGb);
            } else {
                BoxOfficeRequestDTO req = BoxOfficeRequestDTO.builder()
                        .itemPerPage(itemPerPage)
                        .weekGb(weekGb)
                        .build();

                if (includeDaily) {
                    boxOfficeService.fetchAndStoreDailyBoxOffice(req);
                }
                if (includeWeekly) {
                    boxOfficeService.fetchAndStoreWeeklyBoxOffice(req);
                }
            }

            log.info("[BoxOfficeSeed] completed dryRun={} includeDaily={} includeWeekly={} itemPerPage={} weekGb={}",
                    dryRun, includeDaily, includeWeekly, itemPerPage, weekGb);
        } catch (Exception e) {
            exitCode = 1;
            log.error("[BoxOfficeSeed] failed", e);
        } finally {
            int finalExitCode = exitCode;
            SpringApplication.exit(context, () -> finalExitCode);
            System.exit(finalExitCode);
        }
    }
}

