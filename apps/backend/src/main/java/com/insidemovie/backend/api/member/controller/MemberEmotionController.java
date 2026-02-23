package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.member.service.MemberEmotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/v1/members")
public class MemberEmotionController {
    private final MemberEmotionService memberEmotionService;

    @GetMapping("/me/emotion-summary")
    public ResponseEntity<EmotionAvgDTO> getMyEmotionSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(memberEmotionService.getMyEmotionSummary(userDetails.getUsername()));
    }

    @PostMapping("/{memberId}/emotion-summary")
    public ResponseEntity<MemberEmotionSummaryResponseDTO> createInitialEmotionSummary(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    ) {
        MemberEmotionSummaryResponseDTO response = memberEmotionService.saveInitialEmotionSummary(memberId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/me/emotion-summary")
    public ResponseEntity<MemberEmotionSummaryResponseDTO> updateEmotionSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    ) {
        return ResponseEntity.ok(memberEmotionService.updateEmotionSummary(userDetails.getUsername(), request));
    }
}
