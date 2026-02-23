package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.member.dto.MemberLoginRequestDto;
import com.insidemovie.backend.api.member.dto.MemberLoginResponseDto;
import com.insidemovie.backend.api.member.dto.TokenResponseDto;
import com.insidemovie.backend.api.member.service.MemberService;
import com.insidemovie.backend.api.member.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private final MemberService memberService;
    private final OAuthService oAuthService;

    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        return memberService.login(request);
    }

    public TokenResponseDto reissue(String refreshToken) {
        return memberService.reissue(refreshToken);
    }

    public TokenResponseDto kakaoLogin(String kakaoAccessToken) {
        return memberService.kakaoLogin(kakaoAccessToken);
    }

    public void logout(Long userId) {
        memberService.logout(userId);
    }

    public String getKakaoAccessToken(String code) {
        return oAuthService.getKakaoAccessToken(code);
    }
}
