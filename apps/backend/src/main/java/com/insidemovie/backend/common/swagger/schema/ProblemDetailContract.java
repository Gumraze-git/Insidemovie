package com.insidemovie.backend.common.swagger.schema;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "RFC7807 ProblemDetail payload")
public class ProblemDetailContract {
    @Schema(description = "Problem type URI", example = "https://insidemovie.dev/problems/unauthorized")
    private String type;

    @Schema(description = "Problem title", example = "Unauthorized")
    private String title;

    @Schema(description = "HTTP status code", example = "401")
    private Integer status;

    @Schema(description = "Detail message", example = "Authentication is required")
    private String detail;

    @Schema(description = "Request URI", example = "/api/v1/users/me")
    private String instance;

    @Schema(description = "Domain error code", example = "UNAUTHORIZED")
    private String code;

    @Schema(description = "Error timestamp in UTC ISO-8601 format", example = "2026-02-23T00:00:00Z")
    private String timestamp;

    @Schema(description = "Trace id for request correlation", example = "7c58f76a-6421-4b08-9c7e-e4bc1b9c6cb2")
    private String traceId;

    @ArraySchema(arraySchema = @Schema(description = "Validation errors"), schema = @Schema(implementation = ValidationErrorItemContract.class))
    private List<ValidationErrorItemContract> errors;
}

