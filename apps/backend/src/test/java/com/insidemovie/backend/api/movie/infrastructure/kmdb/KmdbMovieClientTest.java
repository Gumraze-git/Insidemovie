package com.insidemovie.backend.api.movie.infrastructure.kmdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.movie.infrastructure.kmdb.model.KmdbMovieCandidate;
import com.insidemovie.backend.common.config.KmdbApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KmdbMovieClientTest {

    private MockRestServiceServer mockServer;
    private KmdbMovieClient kmdbMovieClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("http://kmdb.test");
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        KmdbApiProperties kmdbApiProperties = new KmdbApiProperties();
        kmdbApiProperties.setKey("test-key");

        kmdbMovieClient = new KmdbMovieClient(
                restClientBuilder.build(),
                kmdbApiProperties,
                new ObjectMapper()
        );
    }

    @Test
    void searchMovieCandidatesShouldUseSingleEncodingAndYearRange() {
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> {
                    URI uri = request.getURI();
                    Map<String, String> query = parseQuery(uri);

                    assertThat(uri.getPath()).isEqualTo("/search_api/search_json2.jsp");
                    assertThat(query.get("collection")).isEqualTo("kmdb_new2");
                    assertThat(query.get("ServiceKey")).isEqualTo("test-key");
                    assertThat(query.get("title")).isEqualTo("윗집 사람들");
                    assertThat(query.get("director")).isEqualTo("하정우");
                    assertThat(query.get("releaseDts")).isEqualTo("20250101");
                    assertThat(query.get("releaseDte")).isEqualTo("20251231");
                    assertThat(uri.getRawQuery()).doesNotContain("%25EC%259C%2597");
                })
                .andRespond(withSuccess(
                        "{\"Data\":[{\"TotalCount\":0,\"Result\":[]}]}",
                        MediaType.APPLICATION_JSON
                ));

        kmdbMovieClient.searchMovieCandidates("윗집 사람들", 2025, "하정우", 20);

        mockServer.verify();
    }

    @Test
    void searchMovieCandidatesShouldNormalizeSpecialTitleAndParseResult() {
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> {
                    URI uri = request.getURI();
                    Map<String, String> query = parseQuery(uri);

                    assertThat(query.get("title")).isEqualTo("2024 12 03 그날 조작된 내란 감춰진 진실");
                    assertThat(query.get("releaseDts")).isEqualTo("20260101");
                    assertThat(query.get("releaseDte")).isEqualTo("20261231");
                    assertThat(uri.getRawQuery()).doesNotContain("%2520");
                })
                .andRespond(withSuccess("""
                        {
                          "Data": [
                            {
                              "TotalCount": "1",
                              "Result": [
                                {
                                  "title": " !HS 2024.12.03 그날 조작된 내란, 감춰진 진실 !HE ",
                                  "titleEng": "The Day Hidden",
                                  "prodYear": "2026",
                                  "directors": {
                                    "director": [
                                      { "directorNm": "!HS 하정우 !HE" }
                                    ]
                                  },
                                  "posters": "http://poster.example/main.jpg|http://poster.example/backup.jpg",
                                  "stlls": "http://still.example/main.jpg",
                                  "plots": {
                                    "plot": [
                                      { "plotText": "!HS 줄거리 !HE 테스트" }
                                    ]
                                  }
                                }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<KmdbMovieCandidate> candidates = kmdbMovieClient.searchMovieCandidates(
                "2024.12.03 그날 조작된 내란, 감춰진 진실",
                2026,
                "",
                20
        );

        assertThat(candidates).hasSize(1);
        KmdbMovieCandidate candidate = candidates.get(0);
        assertThat(candidate.title()).isEqualTo("2024.12.03 그날 조작된 내란, 감춰진 진실");
        assertThat(candidate.productionYear()).isEqualTo(2026);
        assertThat(candidate.directors()).containsExactly("하정우");
        assertThat(candidate.posterPath()).isEqualTo("http://poster.example/main.jpg");
        assertThat(candidate.backdropPath()).isEqualTo("http://still.example/main.jpg");
        assertThat(candidate.overview()).isEqualTo("줄거리 테스트");

        mockServer.verify();
    }

    @Test
    void searchMovieCandidatesShouldKeepEnglishWhitespaceWithoutDoubleEncoding() {
        mockServer.expect(anything())
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> {
                    URI uri = request.getURI();
                    Map<String, String> query = parseQuery(uri);

                    assertThat(query.get("title")).isEqualTo("The Life of Chuck");
                    assertThat(query).doesNotContainKeys("releaseDts", "releaseDte");
                    assertThat(uri.getRawQuery()).doesNotContain("%2520");
                })
                .andRespond(withSuccess(
                        "{\"Data\":[{\"TotalCount\":0,\"Result\":[]}]}",
                        MediaType.APPLICATION_JSON
                ));

        List<KmdbMovieCandidate> candidates = kmdbMovieClient.searchMovieCandidates(
                "The Life of Chuck",
                null,
                "",
                20
        );

        assertThat(candidates).isEmpty();
        mockServer.verify();
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> queryMap = new LinkedHashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return queryMap;
        }

        for (String token : rawQuery.split("&")) {
            String[] pair = token.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            queryMap.put(key, value);
        }
        return queryMap;
    }
}
