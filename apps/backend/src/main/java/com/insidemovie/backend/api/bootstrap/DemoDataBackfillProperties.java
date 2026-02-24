package com.insidemovie.backend.api.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "demo.data.backfill")
public class DemoDataBackfillProperties {
    private boolean enabled = false;
    private boolean dryRun = false;
    private boolean includeAccounts = true;
    private boolean includeGenres = true;
    private boolean includeMetadata = true;
    private boolean includeReviews = true;
    private boolean includeMatches = true;
    private final Review review = new Review();
    private final Match match = new Match();

    @Getter
    @Setter
    public static class Review {
        private int targetPerAccount = 20;
        private boolean includeWebFallback = true;
    }

    @Getter
    @Setter
    public static class Match {
        private int closedTargetCount = 8;
        private int currentVoteTarget = 10;
    }
}

