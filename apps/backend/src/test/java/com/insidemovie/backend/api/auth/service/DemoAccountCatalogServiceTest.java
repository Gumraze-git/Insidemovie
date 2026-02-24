package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.auth.dto.DemoAccountOptionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAccountCatalogServiceTest {

    @Test
    void shouldProvideFiveOnboardingAndThirtyGeneralAccounts() {
        DemoAccountCatalogService service = new DemoAccountCatalogService();

        List<DemoAccountOptionResponse> options = service.getAccountOptions();
        long onboardingCount = options.stream()
                .filter(option -> "ONBOARDING".equals(option.getCategory()))
                .count();
        long generalCount = options.stream()
                .filter(option -> "GENERAL".equals(option.getCategory()))
                .count();

        assertThat(options).hasSize(35);
        assertThat(onboardingCount).isEqualTo(5);
        assertThat(generalCount).isEqualTo(30);
        assertThat(options.get(0).getAccountKey()).isEqualTo("onboarding-01");
    }
}
