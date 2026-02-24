package com.insidemovie.backend.common.config.security;

import com.insidemovie.backend.api.jwt.JwtProperties;
import com.insidemovie.backend.common.problem.ProblemDetailResponseWriter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {
    private static final String[] DOCS_ENDPOINTS = {
            "/h2-console/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/api-doc"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/users",
            "/api/v1/users/social/kakao",
            "/api/v1/auth/sessions",
            "/api/v1/auth/sessions/refresh",
            "/api/v1/auth/demo-sessions",
            "/api/v1/auth/providers/kakao/token-exchanges",
            "/api/v1/auth/providers/kakao/sessions",
            "/api/v1/users/*/emotion-summary",
            "/api/v1/movie-recommendations/by-emotion",
            "/api/v1/email-verifications",
            "/api/v1/email-verifications/confirm"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/movies/search",
            "/api/v1/movies/search/**",
            "/api/v1/movies/popular",
            "/api/v1/movies/recommend/**",
            "/api/v1/movies/*",
            "/api/v1/movies/*/emotions",
            "/api/v1/movies/*/emotion-summary",
            "/api/v1/movies/*/reviews",
            "/api/v1/matches/current",
            "/api/v1/matches/winners",
            "/api/v1/boxoffice/**",
            "/api/v1/users/nickname-availability",
            "/api/v1/auth/demo-accounts"
    };

    private static final List<String> CORS_ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:8000",
            "http://52.79.175.149:5173",
            "http://52.79.175.149:8000"
    );

    private static final List<String> CORS_EXPOSED_HEADERS = List.of(
            "Authorization",
            "Authorization-Refresh",
            "Location",
            "X-Trace-Id"
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtProperties jwtProperties,
            ProblemDetailResponseWriter problemWriter
    ) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(DOCS_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/*/reports").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenResolver(bearerTokenResolver(jwtProperties))
                )
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                problemWriter.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "UNAUTHORIZED",
                                        "Authentication is required",
                                        request
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                problemWriter.write(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "FORBIDDEN",
                                        "Access is denied",
                                        request
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        SecretKey key = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver(JwtProperties jwtProperties) {
        return request -> resolveAccessTokenFromCookie(request, jwtProperties.getCookie().getAccessName());
    }

    private String resolveAccessTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("auth");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(CORS_ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.setExposedHeaders(CORS_EXPOSED_HEADERS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
