package com.insidemovie.backend.api.bootstrap;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewAiSeedReport {
    private final int requestedReviews;
    private final int createdReviews;
    private final int skippedReviews;
    private final int failedReviews;
}

