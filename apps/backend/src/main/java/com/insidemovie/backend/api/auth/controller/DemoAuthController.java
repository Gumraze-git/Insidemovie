package com.insidemovie.backend.api.auth.controller;

import com.insidemovie.backend.api.auth.dto.AuthSessionResponse;
import com.insidemovie.backend.api.auth.dto.DemoAccountListResponse;
import com.insidemovie.backend.api.auth.dto.DemoSessionCreateRequest;
import com.insidemovie.backend.api.auth.service.AuthCookieService;
import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.auth.service.DemoSessionService;
import com.insidemovie.backend.api.member.dto.MemberLoginResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(name = "demo.accounts.enabled", havingValue = "true")
public class DemoAuthController {
    private final DemoAccountCatalogService demoAccountCatalogService;
    private final DemoSessionService demoSessionService;
    private final AuthCookieService authCookieService;

    @GetMapping("/demo-accounts")
    public ResponseEntity<DemoAccountListResponse> getDemoAccounts() {
        return ResponseEntity.ok(new DemoAccountListResponse(demoAccountCatalogService.getAccountOptions()));
    }

    @PostMapping("/demo-sessions")
    public ResponseEntity<AuthSessionResponse> createDemoSession(
            @Valid @RequestBody DemoSessionCreateRequest request
    ) {
        MemberLoginResponseDto loginResponse =
                demoSessionService.createSessionByAccountKey(request.getAccountKey().trim());

        HttpHeaders headers = authCookieService.buildAuthCookies(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(AuthSessionResponse.builder()
                        .authority(loginResponse.getAuthority().name())
                        .authenticated(true)
                        .refreshed(false)
                        .build());
    }
}
