package com.insidemovie.backend.api.auth.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DemoAccountSeedReport {
    private final int totalDefinitions;
    private final int createdMembers;
    private final int updatedMembers;
    private final int createdEmotionSummaries;
    private final int updatedEmotionSummaries;
}

