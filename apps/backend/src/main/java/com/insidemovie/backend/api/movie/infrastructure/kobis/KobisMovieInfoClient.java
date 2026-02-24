package com.insidemovie.backend.api.movie.infrastructure.kobis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.insidemovie.backend.api.movie.infrastructure.kobis.model.KobisMovieInfo;
import com.insidemovie.backend.common.config.KobisApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class KobisMovieInfoClient {

    private static final String MOVIE_INFO_PATH_JSON = "/movie/searchMovieInfo.json";

    @Qualifier("kobisRestClient")
    private final RestClient kobisRestClient;
    private final KobisApiProperties kobisApiProperties;

    public Optional<KobisMovieInfo> fetchMovieInfo(String movieCd) {
        if (movieCd == null || movieCd.isBlank()) {
            return Optional.empty();
        }

        String uri = UriComponentsBuilder.fromPath(MOVIE_INFO_PATH_JSON)
                .queryParam("key", kobisApiProperties.getKey())
                .queryParam("movieCd", movieCd)
                .toUriString();

        JsonNode response;
        try {
            response = kobisRestClient.get().uri(uri).retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("KOBIS movieInfo request failed movieCd={} error={}", movieCd, e.getMessage());
            return Optional.empty();
        }

        JsonNode movieInfo = Optional.ofNullable(response)
                .orElseGet(JsonNodeFactory.instance::objectNode)
                .path("movieInfoResult")
                .path("movieInfo");

        if (movieInfo.isMissingNode() || movieInfo.isNull() || movieInfo.isEmpty()) {
            return Optional.empty();
        }

        List<String> directors = toList(movieInfo.path("directors"), "director", "peopleNm");
        List<String> actors = toList(movieInfo.path("actors"), "actor", "peopleNm");
        List<String> genres = toList(movieInfo.path("genres"), "genre", "genreNm");
        String nation = toJoinedList(movieInfo.path("nations"), "nation", "nationNm");

        JsonNode auditArray = movieInfo.path("audits").path("audit");
        if (!auditArray.isArray() && movieInfo.path("audits").isArray()) {
            auditArray = movieInfo.path("audits");
        }

        String rating = StreamSupport.stream(auditArray.spliterator(), false)
                .map(node -> text(node, "watchGradeNm"))
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse("");

        Integer productionYear = parseInteger(text(movieInfo, "prdtYear"));
        Integer runtime = parseInteger(text(movieInfo, "showTm"));

        return Optional.of(new KobisMovieInfo(
                movieCd,
                text(movieInfo, "movieNm"),
                text(movieInfo, "movieNmEn"),
                text(movieInfo, "openDt"),
                productionYear,
                runtime,
                nation,
                directors,
                actors,
                rating,
                genres
        ));
    }

    private List<String> toList(JsonNode parent, String arrayField, String valueField) {
        JsonNode array = parent.path(arrayField);
        if (!array.isArray() && parent.isArray()) {
            array = parent;
        }
        return StreamSupport.stream(array.spliterator(), false)
                .map(node -> text(node, valueField))
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String toJoinedList(JsonNode parent, String arrayField, String valueField) {
        return toList(parent, arrayField, valueField).stream().collect(Collectors.joining(", "));
    }

    private Integer parseInteger(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}
