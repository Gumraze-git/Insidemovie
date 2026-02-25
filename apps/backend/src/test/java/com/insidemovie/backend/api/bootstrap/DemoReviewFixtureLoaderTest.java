package com.insidemovie.backend.api.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoReviewFixtureLoaderTest {

    @Test
    void mainFixtureShouldContainSixHundredValidRows() {
        DemoReviewFixtureLoader loader = new DemoReviewFixtureLoader(new ObjectMapper().findAndRegisterModules());

        DemoReviewFixtureLoadResult result = loader.load("seed/demo-reviews.v1.jsonl", 260);

        assertThat(result.rows()).hasSize(600);
        assertThat(result.invalidRows()).isZero();
    }

    @Test
    void loadShouldValidateAndFilterInvalidRows() {
        DemoReviewFixtureLoader loader = new DemoReviewFixtureLoader(new ObjectMapper().findAndRegisterModules());

        DemoReviewFixtureLoadResult result = loader.load("seed/demo-review-fixture-test.jsonl", 260);

        assertThat(result.rows()).hasSize(2);
        assertThat(result.invalidRows()).isEqualTo(3);
    }
}
