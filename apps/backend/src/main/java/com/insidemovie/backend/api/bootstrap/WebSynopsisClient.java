package com.insidemovie.backend.api.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSynopsisClient {

    private static final String KO_WIKI_SEARCH_URL =
            "https://ko.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=1&srsearch={query}";
    private static final String EN_WIKI_SEARCH_URL =
            "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=1&srsearch={query}";
    private static final String KO_WIKI_SUMMARY_URL = "https://ko.wikipedia.org/api/rest_v1/page/summary/{title}";
    private static final String EN_WIKI_SUMMARY_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/{title}";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public Optional<String> searchSynopsis(String title, String titleEn) {
        Optional<String> ko = lookupWithWikipedia(title, KO_WIKI_SEARCH_URL, KO_WIKI_SUMMARY_URL);
        if (ko.isPresent()) {
            return ko;
        }

        if (titleEn != null && !titleEn.isBlank()) {
            Optional<String> en = lookupWithWikipedia(titleEn, EN_WIKI_SEARCH_URL, EN_WIKI_SUMMARY_URL);
            if (en.isPresent()) {
                return en;
            }
        }
        return Optional.empty();
    }

    private Optional<String> lookupWithWikipedia(String query, String searchUrlTemplate, String summaryUrlTemplate) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        RestClient client = restClientBuilder.clone().build();
        try {
            String encodedQuery = UriUtils.encodeQueryParam(query.trim() + " 영화 줄거리", StandardCharsets.UTF_8);
            String searchRaw = client.get()
                    .uri(searchUrlTemplate, encodedQuery)
                    .retrieve()
                    .body(String.class);
            if (searchRaw == null || searchRaw.isBlank()) {
                return Optional.empty();
            }

            JsonNode searchNode = objectMapper.readTree(searchRaw);
            JsonNode first = searchNode.path("query").path("search");
            if (!first.isArray() || first.isEmpty()) {
                return Optional.empty();
            }

            String pageTitle = first.get(0).path("title").asText("");
            if (pageTitle.isBlank()) {
                return Optional.empty();
            }

            String encodedTitle = UriUtils.encodePathSegment(pageTitle, StandardCharsets.UTF_8);
            String summaryRaw = client.get()
                    .uri(summaryUrlTemplate, encodedTitle)
                    .retrieve()
                    .body(String.class);
            if (summaryRaw == null || summaryRaw.isBlank()) {
                return Optional.empty();
            }

            JsonNode summaryNode = objectMapper.readTree(summaryRaw);
            String extract = summaryNode.path("extract").asText("").trim();
            if (extract.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(extract);
        } catch (Exception e) {
            log.debug("Wikipedia synopsis lookup failed query={} error={}", query, e.getMessage());
            return Optional.empty();
        }
    }
}

