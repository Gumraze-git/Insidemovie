package com.insidemovie.backend.api.admin.docs;

import com.insidemovie.backend.api.admin.dto.AdminDashboardDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberStatusUpdateRequest;
import com.insidemovie.backend.api.admin.dto.AdminReportDTO;
import com.insidemovie.backend.api.admin.dto.AdminReportStatusUpdateRequest;
import com.insidemovie.backend.common.response.PageResult;
import com.insidemovie.backend.common.swagger.annotation.ApiCommonErrorResponses;
import com.insidemovie.backend.common.swagger.annotation.ApiCookieAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin", description = "Admin management APIs")
@ApiCookieAuth
@ApiCommonErrorResponses
public interface AdminManagementApi {

    @Operation(summary = "Get user list")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResult<AdminMemberDTO>> getUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Update user banned status")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody AdminMemberStatusUpdateRequest request
    );

    @Operation(summary = "Get report list")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<PageResult<AdminReportDTO>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Update report status")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<Void> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportStatusUpdateRequest request
    );

    @Operation(summary = "Get admin dashboard summary")
    @ApiResponse(responseCode = "200", description = "OK")
    ResponseEntity<AdminDashboardDTO> getDashboard();
}
