package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import com.insidemovie.backend.api.member.repository.MemberEmotionSummaryRepository;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DemoAccountSeedService {
    private static final String DEMO_PASSWORD = "DemoA!1234";

    private final DemoAccountCatalogService demoAccountCatalogService;
    private final MemberRepository memberRepository;
    private final MemberEmotionSummaryRepository memberEmotionSummaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DemoAccountSeedReport seed(boolean dryRun) {
        int createdMembers = 0;
        int updatedMembers = 0;
        int createdSummaries = 0;
        int updatedSummaries = 0;

        List<DemoAccountCatalogService.DemoAccountDefinition> definitions =
                demoAccountCatalogService.getAccountDefinitions();

        for (DemoAccountCatalogService.DemoAccountDefinition definition : definitions) {
            Optional<Member> existing = memberRepository.findByEmail(definition.email());
            Member member;
            boolean createMember = existing.isEmpty();

            if (createMember) {
                member = Member.builder()
                        .email(definition.email())
                        .password(passwordEncoder.encode(DEMO_PASSWORD))
                        .nickname(definition.nickname())
                        .socialType("NORMAL")
                        .socialId(null)
                        .authority(Authority.ROLE_USER)
                        .reportCount(0)
                        .isBanned(false)
                        .build();

                if (!dryRun) {
                    member = memberRepository.save(member);
                }
                createdMembers++;
            } else {
                member = existing.get();
                member.updateNickname(definition.nickname());
                member.setAuthority(Authority.ROLE_USER);
                member.setBanned(false);
                member.setSocialType("NORMAL");
                member.setSocialId(null);
                if (member.getPassword() == null || member.getPassword().isBlank()) {
                    member.updatePassword(passwordEncoder.encode(DEMO_PASSWORD));
                }
                if (!dryRun) {
                    memberRepository.save(member);
                }
                updatedMembers++;
            }

            Optional<MemberEmotionSummary> existingSummary =
                    createMember && dryRun ? Optional.empty() : memberEmotionSummaryRepository.findByMemberId(member.getId());

            final Member finalMember = member;
            MemberEmotionSummary summary = existingSummary
                    .orElseGet(() -> MemberEmotionSummary.builder().member(finalMember).build());

            if (summary.getMember() == null) {
                summary.setMember(member);
            }

            summary.updateFromDTO(EmotionAvgDTO.builder()
                    .joy((double) definition.joy())
                    .sadness((double) definition.sadness())
                    .anger((double) definition.anger())
                    .fear((double) definition.fear())
                    .disgust((double) definition.disgust())
                    .repEmotionType(definition.representativeEmotion())
                    .build());

            if (existingSummary.isPresent()) {
                updatedSummaries++;
            } else {
                createdSummaries++;
            }

            if (!dryRun) {
                memberEmotionSummaryRepository.save(summary);
            }
        }

        return DemoAccountSeedReport.builder()
                .totalDefinitions(definitions.size())
                .createdMembers(createdMembers)
                .updatedMembers(updatedMembers)
                .createdEmotionSummaries(createdSummaries)
                .updatedEmotionSummaries(updatedSummaries)
                .build();
    }
}
