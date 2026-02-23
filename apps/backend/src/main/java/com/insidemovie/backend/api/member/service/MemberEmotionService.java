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

    public EmotionAvgDTO getMyEmotionSummary(Long userId) {
        return memberService.getMyEmotionSummary(userId);
    }

    public MemberEmotionSummaryResponseDTO saveInitialEmotionSummary(
            Long userId,
            MemberEmotionSummaryRequestDTO request
    ) {
        request.setUserId(userId);
        return memberService.saveInitialEmotionSummary(request);
    }

    public MemberEmotionSummaryResponseDTO updateEmotionSummary(
            Long userId,
            MemberEmotionSummaryRequestDTO request
    ) {
        return memberService.updateEmotionSummary(userId, request);
    }
}
