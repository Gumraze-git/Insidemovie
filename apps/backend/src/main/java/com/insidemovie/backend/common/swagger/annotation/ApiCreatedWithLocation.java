package com.insidemovie.backend.common.swagger.annotation;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "201",
        description = "Created",
        headers = {
                @Header(
                        name = "Location",
                        description = "URI of created resource",
                        schema = @Schema(type = "string")
                )
        }
)
public @interface ApiCreatedWithLocation {
}

