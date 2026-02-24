package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.GenreType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GenreMappingService {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[,/|·]");

    private static final Map<String, GenreType> ALIAS = new LinkedHashMap<>();

    static {
        ALIAS.put("멜로", GenreType.로맨스);
        ALIAS.put("멜로로맨스", GenreType.로맨스);
        ALIAS.put("로맨틱코미디", GenreType.로맨스);
        ALIAS.put("서스펜스", GenreType.스릴러);
        ALIAS.put("공상과학", GenreType.SF);
        ALIAS.put("다큐", GenreType.다큐멘터리);
        ALIAS.put("가족영화", GenreType.가족);
        ALIAS.put("호러", GenreType.공포);
        ALIAS.put("무협", GenreType.액션);
    }

    public Set<GenreType> mapGenres(List<String> sourceGenres) {
        if (sourceGenres == null || sourceGenres.isEmpty()) {
            return Set.of();
        }

        Set<GenreType> mapped = new LinkedHashSet<>();
        for (String raw : sourceGenres) {
            for (String token : split(raw)) {
                mapSingle(token).ifPresent(mapped::add);
            }
        }

        return mapped;
    }

    private Optional<GenreType> mapSingle(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String trimmed = raw.trim();
        Optional<GenreType> direct = GenreType.fromName(trimmed);
        if (direct.isPresent()) {
            return direct;
        }

        String normalized = normalize(trimmed);
        if (normalized.equals("sf") || normalized.equals("scifi")) {
            return Optional.of(GenreType.SF);
        }

        GenreType alias = ALIAS.get(normalized);
        if (alias != null) {
            return Optional.of(alias);
        }

        log.debug("Unmapped KOBIS genre token={}", raw);
        return Optional.empty();
    }

    private List<String> split(String raw) {
        List<String> tokens = new ArrayList<>();
        for (String token : SPLIT_PATTERN.split(raw)) {
            String trimmed = token.trim();
            if (!trimmed.isBlank()) {
                tokens.add(trimmed);
            }
        }
        if (tokens.isEmpty()) {
            tokens.add(raw);
        }
        return tokens;
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "")
                .replaceAll("[()\\-]", "")
                .toLowerCase(Locale.ROOT);
    }
}
