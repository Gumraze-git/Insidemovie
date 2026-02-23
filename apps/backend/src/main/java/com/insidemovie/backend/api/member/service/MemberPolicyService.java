package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.common.exception.ForbiddenException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberPolicyService {
    private final MemberRepository memberRepository;

    public Member getMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
    }

    public Member getActiveMemberById(Long userId) {
        Member member = getMemberById(userId);
        if (member.isBanned()) {
            throw new ForbiddenException(ErrorStatus.USER_BANNED_EXCEPTION.getMessage());
        }
        return member;
    }
}
