package com.insidemovie.backend.api.Email.docs;

import com.insidemovie.backend.api.Email.dto.EmailVerificationConfirmRequest;
import com.insidemovie.backend.api.Email.dto.EmailVerificationSendRequest;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Email Verification", description = "Email verification APIs")
@ApiCommonErrorResponses
public interface EmailVerificationApi {

    @Operation(summary = "Send verification code")
    @ApiResponse(responseCode = "202", description = "Accepted")
    ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody EmailVerificationSendRequest request);

    @Operation(summary = "Confirm verification code")
    @ApiNoContent
    ResponseEntity<Void> confirmVerificationCode(@Valid @RequestBody EmailVerificationConfirmRequest request);
}

