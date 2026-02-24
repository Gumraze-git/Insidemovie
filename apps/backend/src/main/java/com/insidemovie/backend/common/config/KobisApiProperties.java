package com.insidemovie.backend.common.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "kobis.api")
public class KobisApiProperties {
    private String key;
    private String baseUrl;
}
