package com.insidemovie.backend.api.bootstrap;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchSeedReport {
    private final int closedMatchesCreated;
    private final int currentMatchesCreated;
    private final int votesCreated;
}

