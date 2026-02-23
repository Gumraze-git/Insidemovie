package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.member.dto.MemberSignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberRegistrationService {
    private final MemberService memberService;

    public Map<String, Object> signup(MemberSignupRequestDto request) {
        return memberService.signup(request);
    }

    public Map<String, Object> kakaoSignup(String accessToken, String nickname) {
        return memberService.kakaoSignup(accessToken, nickname);
    }
}
