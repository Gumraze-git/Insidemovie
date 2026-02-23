package com.insidemovie.backend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoSignupRequestDto {
    @NotBlank
    private String accessToken;

    @NotBlank
    private String nickname;
}
