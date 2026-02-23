package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.member.dto.MemberInfoDto;
import com.insidemovie.backend.api.member.dto.NicknameUpdateRequestDTO;
import com.insidemovie.backend.api.member.dto.PasswordUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileService {
    private final MemberService memberService;

    public MemberInfoDto getMemberInfo(Long userId) {
        return memberService.getMemberInfo(userId);
    }

    public void updateNickname(Long userId, NicknameUpdateRequestDTO request) {
        memberService.updateNickname(userId, request);
    }

    public boolean isNicknameDuplicated(String nickname) {
        return memberService.isNicknameDuplicated(nickname);
    }

    public void updatePassword(Long userId, PasswordUpdateRequestDTO request) {
        memberService.updatePassword(userId, request);
    }
}
