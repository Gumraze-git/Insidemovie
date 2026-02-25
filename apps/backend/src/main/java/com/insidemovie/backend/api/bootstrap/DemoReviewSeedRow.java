package com.insidemovie.backend.api.bootstrap;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DemoReviewSeedRow(
        String accountKey,
        String movieKoficId,
        double rating,
        boolean spoiler,
        LocalDateTime watchedAt,
        String content
) {
}
