package com.insidemovie.backend.api.Email.controller;

import com.insidemovie.backend.api.Email.Service.EmailService;
import com.insidemovie.backend.api.Email.dto.EmailVerificationConfirmRequest;
import com.insidemovie.backend.api.Email.dto.EmailVerificationSendRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email-verifications")
public class EmailVerificationController {
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody EmailVerificationSendRequest request) {
        emailService.sendMail(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmVerificationCode(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.noContent().build();
    }
}
