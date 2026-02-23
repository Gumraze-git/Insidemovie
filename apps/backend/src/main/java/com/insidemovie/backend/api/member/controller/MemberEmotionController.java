package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.member.docs.MemberEmotionApi;
import com.insidemovie.backend.api.member.service.MemberEmotionService;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class MemberEmotionController implements MemberEmotionApi {
    private final MemberEmotionService memberEmotionService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @GetMapping("/me/emotion-summary")
    public ResponseEntity<EmotionAvgDTO> getMyEmotionSummary(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(memberEmotionService.getMyEmotionSummary(currentUserIdResolver.resolve(jwt)));
    }

    @PostMapping("/{userId}/emotion-summary")
    public ResponseEntity<MemberEmotionSummaryResponseDTO> createInitialEmotionSummary(
            @PathVariable Long userId,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    ) {
        MemberEmotionSummaryResponseDTO response = memberEmotionService.saveInitialEmotionSummary(userId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/me/emotion-summary")
    public ResponseEntity<MemberEmotionSummaryResponseDTO> updateEmotionSummary(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    ) {
        return ResponseEntity.ok(memberEmotionService.updateEmotionSummary(currentUserIdResolver.resolve(jwt), request));
    }
}
