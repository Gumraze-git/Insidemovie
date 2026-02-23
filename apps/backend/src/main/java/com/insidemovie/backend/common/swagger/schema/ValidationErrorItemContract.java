package com.insidemovie.backend.common.swagger.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Validation field error")
public class ValidationErrorItemContract {
    @Schema(description = "Invalid field name", example = "code")
    private String field;

    @Schema(description = "Reason why validation failed", example = "must not be blank")
    private String reason;

    @Schema(description = "Rejected value")
    private Object rejectedValue;
}

