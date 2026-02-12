package com.insidemovie.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({FastApiProperties.class, KobisApiProperties.class})
public class RestClientConfig {
    @Bean
    public RestClient tmdbRestClient(
            RestClient.Builder builder,
            @Value("${tmdb.api.base-url}") String tmdbBaseUrl
    ) {
        return builder.clone().baseUrl(tmdbBaseUrl).build();
    }

    @Bean
    public RestClient fastApiRestClient(
            RestClient.Builder builder,
            FastApiProperties fastApiProperties
    ) {
        String baseUrl = fastApiProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return builder.clone().build();
        }
        return builder.clone().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient kobisRestClient(
            RestClient.Builder builder,
            KobisApiProperties kobisApiProperties
    ) {
        return builder.clone().baseUrl(kobisApiProperties.getBaseUrl()).build();
    }

    @Bean
    public RestClient kakaoAuthRestClient(RestClient.Builder builder) {
        return builder.clone().baseUrl("https://kauth.kakao.com").build();
    }

    @Bean
    public RestClient kakaoApiRestClient(RestClient.Builder builder) {
        return builder.clone().baseUrl("https://kapi.kakao.com").build();
    }
}
