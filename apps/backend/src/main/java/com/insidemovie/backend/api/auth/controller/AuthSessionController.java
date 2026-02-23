package com.insidemovie.backend.api.auth.controller;

import com.insidemovie.backend.api.auth.dto.AuthSessionResponse;
import com.insidemovie.backend.api.auth.dto.KakaoSessionRequest;
import com.insidemovie.backend.api.auth.dto.KakaoTokenExchangeRequest;
import com.insidemovie.backend.api.auth.docs.AuthSessionApi;
import com.insidemovie.backend.api.auth.service.AuthCookieService;
import com.insidemovie.backend.api.auth.service.AuthSessionService;
import com.insidemovie.backend.api.jwt.JwtProperties;
import com.insidemovie.backend.api.member.dto.MemberLoginRequestDto;
import com.insidemovie.backend.api.member.dto.MemberLoginResponseDto;
import com.insidemovie.backend.api.member.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthSessionController implements AuthSessionApi {
    private final AuthSessionService authSessionService;
    private final AuthCookieService authCookieService;
    private final JwtProperties jwtProperties;

    @PostMapping("/sessions")
    public ResponseEntity<AuthSessionResponse> createSession(@RequestBody MemberLoginRequestDto request) {
        MemberLoginResponseDto dto = authSessionService.login(request);
        HttpHeaders headers = authCookieService.buildAuthCookies(dto.getAccessToken(), dto.getRefreshToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthSessionResponse.builder()
                        .authority(dto.getAuthority().name())
                        .authenticated(true)
                        .refreshed(false)
                        .build());
    }

    @PostMapping("/sessions/refresh")
    public ResponseEntity<AuthSessionResponse> refreshSession(HttpServletRequest request) {
        String refreshToken = authCookieService.extractTokenFromCookie(
                request,
                jwtProperties.getCookie().getRefreshName(),
                "Refresh token cookie missing"
        );
        TokenResponseDto dto = authSessionService.reissue(refreshToken);
        HttpHeaders headers = authCookieService.buildAuthCookies(dto.getAccessToken(), dto.getRefreshToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthSessionResponse.builder()
                        .authenticated(true)
                        .refreshed(true)
                        .build());
    }

    @DeleteMapping("/sessions/current")
    public ResponseEntity<Void> deleteCurrentSession(@AuthenticationPrincipal UserDetails userDetails) {
        authSessionService.logout(userDetails.getUsername());
        return ResponseEntity.noContent()
                .headers(authCookieService.clearAuthCookies())
                .build();
    }

    @PostMapping("/providers/kakao/token-exchanges")
    public ResponseEntity<Map<String, String>> exchangeKakaoToken(
            @Valid @RequestBody KakaoTokenExchangeRequest request
    ) {
        String token = authSessionService.getKakaoAccessToken(request.getCode());
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @PostMapping("/providers/kakao/sessions")
    public ResponseEntity<AuthSessionResponse> createKakaoSession(@Valid @RequestBody KakaoSessionRequest request) {
        TokenResponseDto dto = authSessionService.kakaoLogin(request.getAccessToken());
        HttpHeaders headers = authCookieService.buildAuthCookies(dto.getAccessToken(), dto.getRefreshToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthSessionResponse.builder()
                        .authenticated(true)
                        .refreshed(false)
                        .build());
    }
}
