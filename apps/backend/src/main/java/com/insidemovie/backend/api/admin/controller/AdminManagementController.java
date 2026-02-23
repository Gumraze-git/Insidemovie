package com.insidemovie.backend.api.admin.controller;

import com.insidemovie.backend.api.admin.dto.AdminDashboardDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberStatusUpdateRequest;
import com.insidemovie.backend.api.admin.dto.AdminReportDTO;
import com.insidemovie.backend.api.admin.dto.AdminReportStatusUpdateRequest;
import com.insidemovie.backend.api.admin.docs.AdminManagementApi;
import com.insidemovie.backend.api.admin.service.AdminService;
import com.insidemovie.backend.api.report.service.ReportService;
import com.insidemovie.backend.common.response.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminManagementController implements AdminManagementApi {
    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/users")
    public ResponseEntity<PageResult<AdminMemberDTO>> getUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminMemberDTO> memberPage = adminService.getMembers(keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResult.of(memberPage));
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody AdminMemberStatusUpdateRequest request
    ) {
        if (request.getBanned()) {
            adminService.banMember(userId);
        } else {
            adminService.unbanMember(userId);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<PageResult<AdminReportDTO>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminReportDTO> reportPage = adminService.getAllReports(PageRequest.of(page, size));
        return ResponseEntity.ok(PageResult.of(reportPage));
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportStatusUpdateRequest request
    ) {
        reportService.updateReportStatus(reportId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardSummary());
    }
}
