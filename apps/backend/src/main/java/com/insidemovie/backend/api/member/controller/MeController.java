package com.insidemovie.backend.api.member.controller;

import com.insidemovie.backend.api.member.dto.MemberInfoDto;
import com.insidemovie.backend.api.member.dto.NicknameCheckResponseDTO;
import com.insidemovie.backend.api.member.dto.NicknameUpdateRequestDTO;
import com.insidemovie.backend.api.member.dto.PasswordUpdateRequestDTO;
import com.insidemovie.backend.api.member.docs.MeApi;
import com.insidemovie.backend.api.member.service.MemberProfileService;
import com.insidemovie.backend.common.config.security.CurrentUserIdResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class MeController implements MeApi {
    private final MemberProfileService memberProfileService;
    private final CurrentUserIdResolver currentUserIdResolver;

    @GetMapping("/me")
    public ResponseEntity<MemberInfoDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(memberProfileService.getMemberInfo(currentUserIdResolver.resolve(jwt)));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<MemberInfoDto> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NicknameUpdateRequestDTO request
    ) {
        memberProfileService.updateNickname(currentUserIdResolver.resolve(jwt), request);
        return ResponseEntity.ok(memberProfileService.getMemberInfo(currentUserIdResolver.resolve(jwt)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PasswordUpdateRequestDTO request
    ) {
        memberProfileService.updatePassword(currentUserIdResolver.resolve(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nickname-availability")
    public ResponseEntity<NicknameCheckResponseDTO> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean duplicated = memberProfileService.isNicknameDuplicated(nickname);
        return ResponseEntity.ok(new NicknameCheckResponseDTO(duplicated));
    }
}
