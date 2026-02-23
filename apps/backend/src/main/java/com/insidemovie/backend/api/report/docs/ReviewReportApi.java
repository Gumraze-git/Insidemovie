package com.insidemovie.backend.api.report.docs;

import com.insidemovie.backend.api.report.dto.ReportCreateRequest;
import com.insidemovie.backend.api.report.dto.ReportResponseDTO;
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

@Tag(name = "Report", description = "Review report APIs")
@ApiCommonErrorResponses
public interface ReviewReportApi {

    @Operation(summary = "Create report for review")
    @ApiCookieAuth
    @ApiCreatedWithLocation
    ResponseEntity<ReportResponseDTO> createReport(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReportCreateRequest request
    );

    @Operation(summary = "Get report")
    @ApiCookieAuth
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<ReportResponseDTO> getReport(@PathVariable Long reportId);
}

