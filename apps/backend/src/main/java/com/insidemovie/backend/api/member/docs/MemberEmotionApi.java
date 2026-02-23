package com.insidemovie.backend.api.member.docs;

import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryResponseDTO;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Emotion", description = "User emotion summary APIs")
@ApiCommonErrorResponses
public interface MemberEmotionApi {

    @Operation(summary = "Get my emotion summary")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<EmotionAvgDTO> getMyEmotionSummary(@AuthenticationPrincipal Jwt jwt);

    @Operation(summary = "Create initial emotion summary")
    @ApiCreatedWithLocation
    ResponseEntity<MemberEmotionSummaryResponseDTO> createInitialEmotionSummary(
            @PathVariable Long userId,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    );

    @Operation(summary = "Update my emotion summary")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<MemberEmotionSummaryResponseDTO> updateEmotionSummary(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberEmotionSummaryRequestDTO request
    );
}
