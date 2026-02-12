package com.insidemovie.backend.api.member.service;


import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.member.dto.KakaoUserInfoDto;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.InternalServerException;
import com.insidemovie.backend.common.response.ErrorStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    private final ObjectMapper objectMapper;
    @Qualifier("kakaoAuthRestClient")
    private final RestClient kakaoAuthRestClient;
    @Qualifier("kakaoApiRestClient")
    private final RestClient kakaoApiRestClient;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.client.secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    // 인가 코드로 액세스 토큰 요청
    public String getKakaoAccessToken(String code) {
        // 파라미터 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        ResponseEntity<String> response;
        try {
            response = kakaoAuthRestClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientException e) {
            throw new BadRequestException(ErrorStatus.KAKAO_TOKEN_REQUEST_FAILED.getMessage());
        }

        // 액세스 토큰 추출
        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            return responseBody.get("access_token").toString();
        } catch (Exception e) {
            throw new InternalServerException(ErrorStatus.INTERNAL_TOKEN_PARSING_FAILED.getMessage());
        }
    }

    // 액세스 토큰으로 사용자 정보 요청
    public KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        ResponseEntity<String> response;
        try {
            response = kakaoApiRestClient.get()
                    .uri("/v2/user/me")
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientException e) {
            throw new BadRequestException(ErrorStatus.KAKAO_USERINFO_REQUEST_FAILED.getMessage());
        }

        // 사용자 정보 추출
        try {
            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);

            // 프로필 정보 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) result.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            return KakaoUserInfoDto.builder()
                    .id(result.get("id").toString())
                    .email("kakao_" + result.get("id").toString() + "@social.com")
                    .nickname((String) profile.get("nickname"))
                    .build();
        } catch (Exception e) {
            throw new InternalServerException(ErrorStatus.INTERNAL_USERINFO_PARSING_FAILED.getMessage());
        }
    }


}
