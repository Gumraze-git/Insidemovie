package com.insidemovie.backend.api.auth.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.entity.MemberEmotionSummary;
import com.insidemovie.backend.api.member.repository.MemberEmotionSummaryRepository;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@ConditionalOnProperty(name = "demo.accounts.seed-enabled", havingValue = "true")
public class DemoAccountSeedRunner implements CommandLineRunner {
    private static final String DEMO_PASSWORD = "DemoA!1234";

    private final DemoAccountCatalogService demoAccountCatalogService;
    private final MemberRepository memberRepository;
    private final MemberEmotionSummaryRepository memberEmotionSummaryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        int created = 0;
        int updated = 0;

        List<DemoAccountCatalogService.DemoAccountDefinition> definitions =
                demoAccountCatalogService.getAccountDefinitions();

        for (DemoAccountCatalogService.DemoAccountDefinition definition : definitions) {
            Optional<Member> existing = memberRepository.findByEmail(definition.email());
            Member member;

            if (existing.isPresent()) {
                member = existing.get();
                member.updateNickname(definition.nickname());
                member.setAuthority(Authority.ROLE_USER);
                member.setBanned(false);
                member.setSocialType("NORMAL");
                member.setSocialId(null);
                if (member.getPassword() == null || member.getPassword().isBlank()) {
                    member.updatePassword(passwordEncoder.encode(DEMO_PASSWORD));
                }
                updated++;
            } else {
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
                memberRepository.save(member);
                created++;
            }

            MemberEmotionSummary summary = memberEmotionSummaryRepository.findByMemberId(member.getId())
                    .orElseGet(() -> MemberEmotionSummary.builder().member(member).build());
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

            memberEmotionSummaryRepository.save(summary);
        }

        log.info("Demo account seeding completed - total: {}, created: {}, updated: {}",
                definitions.size(), created, updated);
    }
}
