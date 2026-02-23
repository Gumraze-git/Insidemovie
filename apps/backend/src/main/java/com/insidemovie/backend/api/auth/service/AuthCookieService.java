package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.jwt.JwtProperties;
import com.insidemovie.backend.common.exception.UnAuthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {
    private final JwtProperties jwtProperties;

    public HttpHeaders buildAuthCookies(String accessToken, String refreshToken) {
        Duration accessMaxAge = Duration.ofMillis(jwtProperties.getAccessExpMs());
        Duration refreshMaxAge = Duration.ofMillis(jwtProperties.getRefreshExpMs());

        ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getCookie().getAccessName(), accessToken)
                .httpOnly(true)
                .secure(jwtProperties.getCookie().getSecure())
                .sameSite(jwtProperties.getCookie().getSameSite())
                .path("/")
                .maxAge(accessMaxAge)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(jwtProperties.getCookie().getRefreshName(), refreshToken)
                .httpOnly(true)
                .secure(jwtProperties.getCookie().getSecure())
                .sameSite(jwtProperties.getCookie().getSameSite())
                .path("/")
                .maxAge(refreshMaxAge)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    public HttpHeaders clearAuthCookies() {
        ResponseCookie accessCookie = ResponseCookie.from(jwtProperties.getCookie().getAccessName(), "")
                .httpOnly(true)
                .secure(jwtProperties.getCookie().getSecure())
                .sameSite(jwtProperties.getCookie().getSameSite())
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(jwtProperties.getCookie().getRefreshName(), "")
                .httpOnly(true)
                .secure(jwtProperties.getCookie().getSecure())
                .sameSite(jwtProperties.getCookie().getSameSite())
                .path("/")
                .maxAge(0)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    public String extractTokenFromCookie(HttpServletRequest request, String cookieName, String missingMessage) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnAuthorizedException(missingMessage);
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value == null || value.isBlank()) {
                    throw new UnAuthorizedException("Empty token");
                }
                return value;
            }
        }
        throw new UnAuthorizedException(missingMessage);
    }
}
