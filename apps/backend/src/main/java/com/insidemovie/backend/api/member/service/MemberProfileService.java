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

    public MemberInfoDto getMemberInfo(String email) {
        return memberService.getMemberInfo(email);
    }

    public void updateNickname(String email, NicknameUpdateRequestDTO request) {
        memberService.updateNickname(email, request);
    }

    public boolean isNicknameDuplicated(String nickname) {
        return memberService.isNicknameDuplicated(nickname);
    }

    public void updatePassword(String email, PasswordUpdateRequestDTO request) {
        memberService.updatePassword(email, request);
    }
}
