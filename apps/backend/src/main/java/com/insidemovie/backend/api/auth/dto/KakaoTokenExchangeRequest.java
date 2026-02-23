package com.insidemovie.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoTokenExchangeRequest {
    @NotBlank
    private String code;
}
