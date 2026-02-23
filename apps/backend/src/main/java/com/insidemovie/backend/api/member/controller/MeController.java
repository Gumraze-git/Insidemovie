package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.MemberInfoDto;
import com.insidemovie.backend.api.member.dto.NicknameCheckResponseDTO;
import com.insidemovie.backend.api.member.dto.NicknameUpdateRequestDTO;
import com.insidemovie.backend.api.member.dto.PasswordUpdateRequestDTO;
import com.insidemovie.backend.api.member.service.MemberProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MeController {
    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(memberProfileService.getMemberInfo(userDetails.getUsername()));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<MemberInfoDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NicknameUpdateRequestDTO request
    ) {
        memberProfileService.updateNickname(userDetails.getUsername(), request);
        return ResponseEntity.ok(memberProfileService.getMemberInfo(userDetails.getUsername()));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequestDTO request
    ) {
        memberProfileService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nickname-availability")
    public ResponseEntity<NicknameCheckResponseDTO> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean duplicated = memberProfileService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(new NicknameCheckResponseDTO(duplicated));
    }
}
