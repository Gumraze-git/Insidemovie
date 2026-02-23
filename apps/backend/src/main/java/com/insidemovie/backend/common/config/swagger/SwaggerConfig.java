package com.insidemovie.backend.common.config.swagger;

import com.insidemovie.backend.api.jwt.JwtProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    public static final String COOKIE_AUTH_SCHEME_NAME = "cookieAuth";

    @Bean
    public OpenAPI openAPI(JwtProperties jwtProperties) {
        SecurityScheme cookieAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name(jwtProperties.getCookie().getAccessName());

        Server server = new Server();
        server.setUrl("/");

        return new OpenAPI()
                .info(new Info()
                        .title("Inside Movie")
                        .description("Inside Movie REST API Document")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(COOKIE_AUTH_SCHEME_NAME, cookieAuthScheme))
                .addServersItem(server);
    }
}
