package com.insidemovie.backend.api.jwt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 관련 설정값을 외부 프로퍼티로 바인딩한다.
 */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @NotNull
    @Positive
    private Long accessExpMs;

    @NotNull
    @Positive
    private Long refreshExpMs;

    @Valid
    @NotNull
    private Cookie cookie;

    @Getter
    @Setter
    public static class Cookie {
        @NotBlank
        private String accessName;

        @NotBlank
        private String refreshName;

        @NotBlank
        private String sameSite;

        @NotNull
        private Boolean secure;
    }
}
