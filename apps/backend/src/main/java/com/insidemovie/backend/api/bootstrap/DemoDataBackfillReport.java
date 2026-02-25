package com.insidemovie.backend.api.bootstrap;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DemoDataBackfillReport {
    private final int accountsCreated;
    private final int accountsUpdated;
    private final int genreMapped;
    private final int metadataUpdatedPoster;
    private final int metadataUpdatedOverview;
    private final int metadataUpdatedBackdrop;
    private final int reviewsRequested;
    private final int reviewsCreated;
    private final int reviewsSkipped;
    private final int reviewsFailed;
    private final int reviewFixtureLoaded;
    private final int reviewFixtureInvalid;
    private final int emotionsCreated;
    private final int matchesClosedCreated;
    private final int currentCreated;
    private final int votesCreated;
}
