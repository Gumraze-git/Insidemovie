package com.insidemovie.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({
        FastApiProperties.class,
        KobisApiProperties.class,
        KmdbApiProperties.class
})
public class RestClientConfig {
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
    public RestClient kmdbRestClient(
            RestClient.Builder builder,
            KmdbApiProperties kmdbApiProperties
    ) {
        return builder.clone().baseUrl(kmdbApiProperties.getBaseUrl()).build();
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
