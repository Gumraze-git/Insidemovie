package com.insidemovie.backend.common.config.security;

import com.insidemovie.backend.api.jwt.JwtProperties;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Stateless JWT 기반 인증(Authentication) 및 인가(Authorization) 정책을 정의하는 보안 설정 클래스.
 *
 * <p>주요 구성 요소:
 * SecurityFilterChain을 통한 요청별 접근 제어,
 * JwtDecoder를 통한 토큰 검증,
 * JwtAuthenticationConverter를 통한 권한(Authority) 매핑,
 * CORS 정책 적용,
 * 인증 실패 시 401 응답 처리.
 *
 * <p>본 설정은 리소스 서버(Resource Server) 관점의 JWT 검증 흐름을 기준으로 동작한다.
 */
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

    private static final String[] PUBLIC_MEMBER_ENDPOINTS = {
        "/api/v1/member/signup",
        "/api/v1/member/reissue",
        "/api/v1/member/login",
        "/api/v1/member/kakao-accesstoken",
        "/api/v1/member/kakao-login",
        "/api/v1/member/kakao-signup",
        "/api/v1/member/token-reissue",
        "/api/v1/member/check-nickname",
        "/api/v1/match/weekly-match",
        "/api/v1/match/winners"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
        "/api/v1/movies",
        "/api/v1/movies/search",
        "/api/v1/movies/search/**",
        "/api/v1/movies/recommend/**",
        "/api/v1/movies/popular",
        "/api/v1/movies/detail/*",
        "/api/v1/movies/*/emotion-summary",
        "/api/v1/movies/emotions/*",
        "/api/v1/movies/*/reviews",
        "/api/v1/boxoffice/**"
    };

    private static final String[] PUBLIC_MAIL_ENDPOINTS = {
        "/api/v1/mail/send/**",
        "/api/v1/mail/check/**"
    };

    private static final List<String> CORS_ALLOWED_ORIGINS = List.of(
        "http://localhost:5173",
        "http://localhost:8000",
        "http://52.79.175.149:5173",
        "http://52.79.175.149:8000"
    );

    private static final List<String> CORS_EXPOSED_HEADERS = List.of(
        "Authorization",
        "Authorization-Refresh"
    );

    /**
     * 애플리케이션 전역에서 사용할 비밀번호 인코더를 등록한다.
     *
     * @return BCrypt 기반 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인을 구성한다.
     *
     * <p>세션 무상태(STATELESS), 요청별 접근 제어, JWT 리소스 서버 검증, CORS, 예외 처리(401)를 설정한다.
     *
     * @param http Spring Security HttpSecurity 빌더
     * @return 구성된 SecurityFilterChain
     * @throws Exception 필터 체인 생성 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable default form login, HTTP Basic, CSRF
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS configuration
            .cors(Customizer.withDefaults())

            // Allow H2 console frames
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

            // Stateless session (JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // URL access rules
            .authorizeHttpRequests(auth -> auth
                // Public: H2 console, Swagger
                .requestMatchers(DOCS_ENDPOINTS).permitAll()

                // Public: member endpoints
                .requestMatchers(PUBLIC_MEMBER_ENDPOINTS).permitAll()

                // Role-based
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // USER POST
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/report/**"
                ).hasRole("USER")

                // Public GET
                .requestMatchers(
                        HttpMethod.GET,
                        PUBLIC_GET_ENDPOINTS
                ).permitAll()

                // Public POST
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/member/signup/emotion",
                        "/api/v1/recommend/emotion"
                ).permitAll()

                // Public PATCH
                .requestMatchers(
                        HttpMethod.PATCH,
                        "/api/v1/member/emotion/**"
                ).permitAll()
                .requestMatchers(
                        PUBLIC_MAIL_ENDPOINTS).permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Resource Server (JWT)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )

            // Spring Security 로그아웃 기능 비활성화
            .logout(AbstractHttpConfigurer::disable)

            // Return 401 on unauthorized
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint())
            );

        return http.build();
    }

    /**
     * 대칭키(HS512) 기반 JWT 디코더를 생성한다.
     *
     * @param jwtProperties 서명/검증에 사용할 JWT 프로퍼티
     * @return HS512 알고리즘으로 검증하는 JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        SecretKey key = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS512)
            .build();
    }

    /**
     * JWT 클레임을 Spring Security 권한으로 변환하는 컨버터를 생성한다.
     *
     * <p>{@code auth} 클레임 값을 권한(Authority)으로 읽어 인가 규칙에 사용할 수 있도록 매핑한다.
     *
     * @return JWT 권한 매핑이 적용된 JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("auth");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    /**
     * 인증 실패 시 401 Unauthorized 응답을 반환하는 엔트리포인트를 생성한다.
     *
     * @return 401 응답용 AuthenticationEntryPoint
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 정책을 등록한다.
     *
     * <p>허용 Origin, 메서드/헤더, 자격 증명 허용, 노출 헤더, 캐시 시간을 공통 정책으로 설정한다.
     *
     * @return 전역 경로({@code /**})에 적용되는 CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(CORS_ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hour
        config.setExposedHeaders(CORS_EXPOSED_HEADERS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
