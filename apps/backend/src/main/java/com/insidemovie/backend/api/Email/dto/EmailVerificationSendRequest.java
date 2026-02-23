package com.insidemovie.backend.api.Email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationSendRequest {
    @NotBlank
    @Email
    private String email;
}
