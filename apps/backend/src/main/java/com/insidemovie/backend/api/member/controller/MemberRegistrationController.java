package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.KakaoSignupRequestDto;
import com.insidemovie.backend.api.member.dto.MemberSignupRequestDto;
import com.insidemovie.backend.api.member.service.MemberRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberRegistrationController {
    private final MemberRegistrationService memberRegistrationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody MemberSignupRequestDto request) {
        Map<String, Object> result = memberRegistrationService.signup(request);
        Long memberId = ((Number) result.get("memberId")).longValue();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(memberId)
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @PostMapping("/social/kakao")
    public ResponseEntity<Map<String, Object>> kakaoSignup(@Valid @RequestBody KakaoSignupRequestDto request) {
        Map<String, Object> result = memberRegistrationService.kakaoSignup(
                request.getAccessToken(),
                request.getNickname()
        );
        Long memberId = ((Number) result.get("memberId")).longValue();
        URI location = ServletUriComponentsBuilder.fromPath("/api/v1/members/{id}")
                .buildAndExpand(memberId)
                .toUri();
        return ResponseEntity.created(location).body(result);
    }
}
