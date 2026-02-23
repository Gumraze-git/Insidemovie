package com.insidemovie.backend.api.member.docs;

import com.insidemovie.backend.api.member.dto.KakaoSignupRequestDto;
import com.insidemovie.backend.api.member.dto.MemberSignupRequestDto;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCreatedWithLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "Member Registration", description = "Member sign-up APIs")
@ApiCommonErrorResponses
public interface MemberRegistrationApi {

    @Operation(summary = "Sign up by email", description = "Create a new user account.")
    @ApiCreatedWithLocation
    ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody MemberSignupRequestDto request);

    @Operation(summary = "Sign up with Kakao", description = "Create a new Kakao social user account.")
    @ApiCreatedWithLocation
    ResponseEntity<Map<String, Object>> kakaoSignup(@Valid @RequestBody KakaoSignupRequestDto request);
}
