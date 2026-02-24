package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.member.dto.MemberLoginResponseDto;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.jwt.JwtProvider;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoSessionService {
    private final DemoAccountCatalogService demoAccountCatalogService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @Value("${demo.accounts.enabled:false}")
    private boolean demoAccountsEnabled;

    @Transactional
    public MemberLoginResponseDto createSessionByAccountKey(String accountKey) {
        if (!demoAccountsEnabled) {
            throw new BaseException(
                    HttpStatus.FORBIDDEN,
                    "임시 계정 로그인이 비활성화되어 있습니다.",
                    "DEMO_LOGIN_DISABLED"
            );
        }

        DemoAccountCatalogService.DemoAccountDefinition definition =
                demoAccountCatalogService.requireByAccountKey(accountKey);

        Member member = memberRepository.findByEmail(definition.email())
                .orElseThrow(() -> new BaseException(
                        HttpStatus.NOT_FOUND,
                        "임시 계정 사용자를 찾을 수 없습니다.",
                        "DEMO_ACCOUNT_NOT_FOUND"
                ));

        if (member.isBanned()) {
            throw new BaseException(
                    ErrorStatus.USER_BANNED_EXCEPTION.getHttpStatus(),
                    ErrorStatus.USER_BANNED_EXCEPTION.getMessage(),
                    ErrorStatus.USER_BANNED_EXCEPTION.getCode()
            );
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getAuthority().name()));
        String accessToken = jwtProvider.generateAccessToken(member.getId(), authorities);
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());
        member.updateRefreshtoken(refreshToken);

        return MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authority(member.getAuthority())
                .build();
    }
}
