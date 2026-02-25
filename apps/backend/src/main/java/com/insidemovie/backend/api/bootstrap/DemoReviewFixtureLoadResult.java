package com.insidemovie.backend.api.bootstrap;

import java.util.List;

public record DemoReviewFixtureLoadResult(
        List<DemoReviewSeedRow> rows,
        int invalidRows
) {
}
