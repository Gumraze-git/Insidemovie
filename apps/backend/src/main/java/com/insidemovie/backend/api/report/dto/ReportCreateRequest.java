package com.insidemovie.backend.api.report.dto;

import com.insidemovie.backend.api.constant.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCreateRequest {
    @NotNull
    private ReportReason reason;
}
