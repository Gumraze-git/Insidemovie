package com.insidemovie.backend.api.member.service;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberEmotionService {
    private final MemberService memberService;

    public EmotionAvgDTO getMyEmotionSummary(String email) {
        return memberService.getMyEmotionSummary(email);
    }

    public MemberEmotionSummaryResponseDTO saveInitialEmotionSummary(
            Long memberId,
            MemberEmotionSummaryRequestDTO request
    ) {
        request.setMemberId(memberId);
        return memberService.saveInitialEmotionSummary(request);
    }

    public MemberEmotionSummaryResponseDTO updateEmotionSummary(
            String email,
            MemberEmotionSummaryRequestDTO request
    ) {
        return memberService.updateEmotionSummary(email, request);
    }
}
