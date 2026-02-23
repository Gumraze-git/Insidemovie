package com.insidemovie.backend.api.admin.dto;

import com.insidemovie.backend.api.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportStatusUpdateRequest {
    @NotNull
    private ReportStatus status;
}
