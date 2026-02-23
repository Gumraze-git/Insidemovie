package com.insidemovie.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoSessionRequest {
    @NotBlank
    private String accessToken;
}
