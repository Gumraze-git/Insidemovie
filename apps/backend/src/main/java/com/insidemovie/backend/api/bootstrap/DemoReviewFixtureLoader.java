package com.insidemovie.backend.api.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoReviewFixtureLoader {

    private final ObjectMapper objectMapper;

    public DemoReviewFixtureLoadResult load(String fixturePath, int maxContentLen) {
        ClassPathResource resource = new ClassPathResource(fixturePath);
        if (!resource.exists()) {
            throw new IllegalStateException("Review fixture not found: " + fixturePath);
        }

        List<DemoReviewSeedRow> rows = new ArrayList<>();
        Set<String> pairKeys = new HashSet<>();
        Set<String> contentHashes = new HashSet<>();
        int invalidRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
        ) {
            int lineNo = 0;
            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                lineNo++;
                String line = rawLine == null ? "" : rawLine.trim();
                if (line.isEmpty()) {
                    continue;
                }

                DemoReviewSeedRow parsed;
                try {
                    parsed = objectMapper.readValue(line, DemoReviewSeedRow.class);
                } catch (Exception e) {
                    invalidRows++;
                    log.warn("Invalid review fixture JSON line={} path={} error={}", lineNo, fixturePath, e.getMessage());
                    continue;
                }

                String accountKey = normalize(parsed.accountKey());
                String movieKoficId = normalize(parsed.movieKoficId());
                String content = normalizeContent(parsed.content());
                double rating = parsed.rating();
                LocalDateTime watchedAt = parsed.watchedAt();

                if (accountKey.isBlank() || movieKoficId.isBlank() || content.isBlank() || watchedAt == null) {
                    invalidRows++;
                    continue;
                }
                if (rating < 0.5 || rating > 5.0) {
                    invalidRows++;
                    continue;
                }
                if (maxContentLen > 0 && content.length() > maxContentLen) {
                    invalidRows++;
                    continue;
                }

                String pairKey = accountKey + "|" + movieKoficId;
                if (!pairKeys.add(pairKey)) {
                    invalidRows++;
                    continue;
                }

                String contentHash = hash(content);
                if (!contentHashes.add(contentHash)) {
                    invalidRows++;
                    continue;
                }

                rows.add(DemoReviewSeedRow.builder()
                        .accountKey(accountKey)
                        .movieKoficId(movieKoficId)
                        .rating(rating)
                        .spoiler(parsed.spoiler())
                        .watchedAt(watchedAt)
                        .content(content)
                        .build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load review fixture: " + fixturePath, e);
        }

        return new DemoReviewFixtureLoadResult(rows, invalidRows);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private String normalizeContent(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private String hash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash review content", e);
        }
    }
}
