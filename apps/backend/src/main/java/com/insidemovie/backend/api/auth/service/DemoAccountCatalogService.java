package com.insidemovie.backend.api.auth.service;

import com.insidemovie.backend.api.auth.dto.DemoAccountOptionResponse;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.common.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DemoAccountCatalogService {
    private static final String CATEGORY_ONBOARDING = "ONBOARDING";
    private static final String CATEGORY_GENERAL = "GENERAL";

    private final List<DemoAccountDefinition> accountDefinitions;
    private final Map<String, DemoAccountDefinition> accountByKey;

    public DemoAccountCatalogService() {
        List<DemoAccountDefinition> definitions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String number = String.format("%02d", i);
            definitions.add(new DemoAccountDefinition(
                    "onboarding-" + number,
                    "onboarding" + number + "@demo.insidemovie.local",
                    "온보딩" + number,
                    "온보딩 계정 " + number,
                    CATEGORY_ONBOARDING,
                    EmotionType.NONE,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f
            ));
        }

        EmotionType[] cycle = {
                EmotionType.JOY,
                EmotionType.SADNESS,
                EmotionType.ANGER,
                EmotionType.FEAR,
                EmotionType.DISGUST
        };
        for (int i = 1; i <= 30; i++) {
            String number = String.format("%02d", i);
            EmotionType representative = cycle[(i - 1) % cycle.length];
            definitions.add(buildGeneralDefinition(number, representative));
        }

        this.accountDefinitions = List.copyOf(definitions);
        this.accountByKey = definitions.stream()
                .collect(Collectors.toUnmodifiableMap(DemoAccountDefinition::accountKey, Function.identity()));
    }

    public List<DemoAccountDefinition> getAccountDefinitions() {
        return accountDefinitions;
    }

    public List<DemoAccountOptionResponse> getAccountOptions() {
        return accountDefinitions.stream()
                .map(definition -> DemoAccountOptionResponse.builder()
                        .accountKey(definition.accountKey())
                        .label(definition.label())
                        .category(definition.category())
                        .build())
                .toList();
    }

    public DemoAccountDefinition requireByAccountKey(String accountKey) {
        DemoAccountDefinition definition = accountByKey.get(accountKey);
        if (definition == null) {
            throw new BaseException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 임시 계정입니다.",
                    "DEMO_ACCOUNT_NOT_FOUND"
            );
        }
        return definition;
    }

    private DemoAccountDefinition buildGeneralDefinition(String number, EmotionType representative) {
        float joy = 0.095f;
        float sadness = 0.095f;
        float anger = 0.095f;
        float fear = 0.095f;
        float disgust = 0.095f;

        switch (representative) {
            case JOY -> joy = 0.62f;
            case SADNESS -> sadness = 0.62f;
            case ANGER -> anger = 0.62f;
            case FEAR -> fear = 0.62f;
            case DISGUST -> disgust = 0.62f;
            default -> {
            }
        }

        return new DemoAccountDefinition(
                "general-" + number,
                "general" + number + "@demo.insidemovie.local",
                "일반" + number,
                "일반 계정 " + number,
                CATEGORY_GENERAL,
                representative,
                joy,
                sadness,
                anger,
                fear,
                disgust
        );
    }

    public record DemoAccountDefinition(
            String accountKey,
            String email,
            String nickname,
            String label,
            String category,
            EmotionType representativeEmotion,
            float joy,
            float sadness,
            float anger,
            float fear,
            float disgust
    ) {
    }
}
