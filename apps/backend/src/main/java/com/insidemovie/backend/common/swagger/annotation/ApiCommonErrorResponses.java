package com.insidemovie.backend.common.swagger.annotation;

import com.insidemovie.backend.common.swagger.schema.ProblemDetailContract;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Conflict",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        ),
        @ApiResponse(
                responseCode = "503",
                description = "Service Unavailable",
                content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailContract.class))
        )
})
public @interface ApiCommonErrorResponses {
}

