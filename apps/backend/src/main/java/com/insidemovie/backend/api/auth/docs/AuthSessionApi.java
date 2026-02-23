package com.insidemovie.backend.api.auth.docs;

import com.insidemovie.backend.api.auth.dto.AuthSessionResponse;
import com.insidemovie.backend.api.auth.dto.KakaoSessionRequest;
import com.insidemovie.backend.api.auth.dto.KakaoTokenExchangeRequest;
import com.insidemovie.backend.api.member.dto.MemberLoginRequestDto;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Auth", description = "Authentication and session APIs")
@ApiCommonErrorResponses
public interface AuthSessionApi {

    @Operation(summary = "Create login session", description = "Issue auth cookies and return minimum login payload.")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<AuthSessionResponse> createSession(@RequestBody MemberLoginRequestDto request);

    @Operation(summary = "Refresh login session", description = "Rotate access/refresh tokens using refresh cookie.")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<AuthSessionResponse> refreshSession(HttpServletRequest request);

    @Operation(summary = "Delete current session", description = "Logout current user and expire auth cookies.")
    @ApiCookieAuth
    @ApiNoContent
    ResponseEntity<Void> deleteCurrentSession(@AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Exchange Kakao auth code", description = "Exchange Kakao authorization code for Kakao access token.")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Map<String, String>> exchangeKakaoToken(@Valid @RequestBody KakaoTokenExchangeRequest request);

    @Operation(summary = "Create Kakao session", description = "Create service session from Kakao access token.")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<AuthSessionResponse> createKakaoSession(@Valid @RequestBody KakaoSessionRequest request);
}

