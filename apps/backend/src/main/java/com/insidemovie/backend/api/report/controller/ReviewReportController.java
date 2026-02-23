package com.insidemovie.backend.api.report.controller;

import com.insidemovie.backend.api.report.dto.ReportCreateRequest;
import com.insidemovie.backend.api.report.dto.ReportResponseDTO;
import com.insidemovie.backend.api.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewReportController {
    private final ReportService reportService;

    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<ReportResponseDTO> createReport(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        ReportResponseDTO dto = reportService.reportReview(userDetails.getUsername(), reviewId, request.getReason());
        URI location = ServletUriComponentsBuilder.fromPath("/api/v1/reports/{id}")
                .buildAndExpand(dto.getId())
                .toUri();
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ReportResponseDTO> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReportById(reportId));
    }
}
