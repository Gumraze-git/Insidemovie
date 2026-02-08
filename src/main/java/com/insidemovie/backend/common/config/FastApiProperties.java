package com.insidemovie.backend.common.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.fastapi")
public class FastApiProperties {
    private String baseUrl;
    private String url;
}
