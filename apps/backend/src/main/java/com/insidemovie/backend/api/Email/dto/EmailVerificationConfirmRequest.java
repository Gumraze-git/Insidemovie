package com.insidemovie.backend.api.Email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationConfirmRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private Integer code;
}
