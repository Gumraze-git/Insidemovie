package com.insidemovie.backend.api.movie.infrastructure.kmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.model.KmdbMovieCandidate;
import com.insidemovie.backend.common.config.KmdbApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class KmdbMovieClient {

    private static final String KMDB_SEARCH_PATH = "/search_api/search_json2.jsp";
    private static final Pattern MARKUP_PATTERN = Pattern.compile("!HS|!HE|<[^>]+>");

    @Qualifier("kmdbRestClient")
    private final RestClient kmdbRestClient;
    private final KmdbApiProperties kmdbApiProperties;
    private final ObjectMapper objectMapper;

    public List<KmdbMovieCandidate> searchMovieCandidates(String title, Integer year, String director, int listCount) {
        if (title == null || title.isBlank()) {
            return List.of();
        }
        String normalizedTitle = normalizeQuery(title);
        String normalizedDirector = normalizeQuery(director);
        if (normalizedTitle.isBlank()) {
            return List.of();
        }
        int safeListCount = Math.max(1, Math.min(listCount, 20));
        String queryMode = resolveQueryMode(year, normalizedDirector);
        JsonNode response;
        try {
            String raw = kmdbRestClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(KMDB_SEARCH_PATH)
                                .queryParam("collection", "kmdb_new2")
                                .queryParam("ServiceKey", kmdbApiProperties.getKey())
                                .queryParam("detail", "Y")
                                .queryParam("listCount", safeListCount)
                                .queryParam("title", normalizedTitle);
                        if (year != null) {
                            builder.queryParam("releaseDts", String.format(Locale.ROOT, "%04d0101", year));
                            builder.queryParam("releaseDte", String.format(Locale.ROOT, "%04d1231", year));
                        }
                        if (!normalizedDirector.isBlank()) {
                            builder.queryParam("director", normalizedDirector);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .body(String.class);
            if (raw == null || raw.isBlank()) {
                log.debug("KMDb search returned empty body title={} year={} director={} queryMode={}",
                        title, year, director, queryMode);
                return List.of();
            }
            response = objectMapper.readTree(raw);
        } catch (RestClientException e) {
            log.warn("KMDb search request failed title={} year={} director={} queryMode={} error={}",
                    title, year, director, queryMode, e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.warn("KMDb response parse failed title={} year={} director={} queryMode={} error={}",
                    title, year, director, queryMode, e.getMessage());
            return List.of();
        }

        JsonNode dataNode = Optional.ofNullable(response)
                .orElseGet(JsonNodeFactory.instance::objectNode)
                .path("Data");
        int totalCount = extractTotalCount(dataNode);
        log.debug("KMDb search completed title={} year={} director={} queryMode={} totalCount={}",
                title, year, director, queryMode, totalCount);

        if (!dataNode.isArray() || dataNode.isEmpty()) {
            return List.of();
        }

        JsonNode resultArray = dataNode.get(0).path("Result");
        if (!resultArray.isArray() || resultArray.isEmpty()) {
            return List.of();
        }

        return StreamSupport.stream(resultArray.spliterator(), false)
                .map(this::toCandidate)
                .toList();
    }

    private String resolveQueryMode(Integer year, String normalizedDirector) {
        if (year != null && !normalizedDirector.isBlank()) {
            return "TITLE_YEAR_DIRECTOR";
        }
        if (year != null) {
            return "TITLE_YEAR";
        }
        if (!normalizedDirector.isBlank()) {
            return "TITLE_DIRECTOR";
        }
        return "TITLE_ONLY";
    }

    private int extractTotalCount(JsonNode dataNode) {
        if (!dataNode.isArray() || dataNode.isEmpty()) {
            return 0;
        }
        JsonNode totalCountNode = dataNode.get(0).path("TotalCount");
        if (totalCountNode.isInt() || totalCountNode.isLong()) {
            return totalCountNode.asInt(0);
        }
        String raw = totalCountNode.asText("0").trim();
        if (raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private KmdbMovieCandidate toCandidate(JsonNode node) {
        String title = sanitizeMarkedText(text(node, "title"));
        String titleEn = sanitizeMarkedText(text(node, "titleEng"));
        Integer productionYear = parseYear(text(node, "prodYear"));
        List<String> directors = parseDirectors(node);
        String posterPath = firstMediaUrl(text(node, "posters"));
        String backdropPath = firstMediaUrl(text(node, "stlls"));
        String overview = parseOverview(node);

        return new KmdbMovieCandidate(
                title,
                titleEn,
                productionYear,
                directors,
                posterPath,
                backdropPath,
                overview
        );
    }

    private String parseOverview(JsonNode node) {
        JsonNode plots = node.path("plots").path("plot");
        if (!plots.isArray()) {
            return "";
        }

        return StreamSupport.stream(plots.spliterator(), false)
                .map(plot -> sanitizeMarkedText(text(plot, "plotText")))
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("");
    }

    private List<String> parseDirectors(JsonNode node) {
        JsonNode directors = node.path("directors").path("director");
        if (directors.isArray()) {
            return StreamSupport.stream(directors.spliterator(), false)
                    .map(d -> sanitizeMarkedText(text(d, "directorNm")))
                    .filter(name -> !name.isBlank())
                    .distinct()
                    .toList();
        }

        String raw = sanitizeMarkedText(text(node, "director"));
        if (raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,|/]"))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String firstMediaUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("");
    }

    private Integer parseYear(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(digits.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }

    private String sanitizeMarkedText(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return MARKUP_PATTERN.matcher(raw).replaceAll("").replaceAll("\\s+", " ").trim();
    }

    private String normalizeQuery(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replaceAll("!HS|!HE|<[^>]+>", " ")
                .replaceAll("\\([^)]*\\)|\\[[^\\]]*\\]", " ")
                .replaceAll("[:：\\-–_]", " ")
                .replaceAll("[\\p{Punct}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
