package com.insidemovie.backend.api.member.docs;

import com.insidemovie.backend.api.member.dto.MemberInfoDto;
import com.insidemovie.backend.api.member.dto.NicknameCheckResponseDTO;
import com.insidemovie.backend.api.member.dto.NicknameUpdateRequestDTO;
import com.insidemovie.backend.api.member.dto.PasswordUpdateRequestDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiNoContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Me", description = "Current user profile APIs")
@ApiCommonErrorResponses
public interface MeApi {

    @Operation(summary = "Get my profile")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MemberInfoDto> getMe(@AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Update my profile")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MemberInfoDto> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NicknameUpdateRequestDTO request
    );

    @Operation(summary = "Update my password")
    @ApiCookieAuth
    @ApiNoContent
    ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PasswordUpdateRequestDTO request
    );

    @Operation(summary = "Check nickname availability")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<NicknameCheckResponseDTO> checkNicknameDuplicate(@RequestParam String nickname);
}
