package com.insidemovie.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({FastApiProperties.class, KobisApiProperties.class})
public class RestClientConfig {
    @Bean
    public RestClient fastApiRestClient(
            RestClient.Builder builder,
            FastApiProperties fastApiProperties
    ) {
        return builder.baseUrl(fastApiProperties.getBaseUrl()).build();
    }

    @Bean
    public RestClient kobisRestClient(
            RestClient.Builder builder,
            KobisApiProperties kobisApiProperties
    ) {
        return builder.baseUrl(kobisApiProperties.getBaseUrl()).build();
    }
}
