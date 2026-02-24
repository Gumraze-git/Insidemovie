package com.insidemovie.backend.api.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoDataBackfillProperties.class)
public class DemoDataBackfillConfig {
}

