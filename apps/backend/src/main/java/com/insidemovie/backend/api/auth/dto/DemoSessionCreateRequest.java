package com.insidemovie.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DemoSessionCreateRequest {
    @NotBlank(message = "accountKey를 입력해주세요.")
    private String accountKey;
}
