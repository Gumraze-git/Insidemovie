package com.insidemovie.backend.common.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class OpenApiContractCustomiser {
    private static final List<String> ERROR_CODES = List.of("400", "401", "403", "404", "409", "500", "503");
    private static final String PROBLEM_SCHEMA_NAME = "ProblemDetailContract";
    private static final String VALIDATION_ERROR_SCHEMA_NAME = "ValidationErrorItemContract";
    private static final String PROBLEM_SCHEMA_REF = "#/components/schemas/" + PROBLEM_SCHEMA_NAME;
    private static final String PROBLEM_RESPONSE_PREFIX = "Problem";

    @Bean
    public OpenApiCustomizer openApiContractCustomizer() {
        return openApi -> {
            ensureCommonProblemSchemas(openApi);
            ensureCommonProblemResponses(openApi);
            normalizeOperations(openApi);
            sortOpenApi(openApi);
        };
    }

    private void ensureCommonProblemSchemas(OpenAPI openApi) {
        Components components = getOrCreateComponents(openApi);
        if (!components.getSchemas().containsKey(VALIDATION_ERROR_SCHEMA_NAME)) {
            Schema<?> validationErrorSchema = new ObjectSchema()
                    .addProperty("field", new StringSchema())
                    .addProperty("reason", new StringSchema())
                    .addProperty("rejectedValue", new ObjectSchema());
            components.addSchemas(VALIDATION_ERROR_SCHEMA_NAME, validationErrorSchema);
        }

        if (!components.getSchemas().containsKey(PROBLEM_SCHEMA_NAME)) {
            Schema<?> problemSchema = new ObjectSchema()
                    .addProperty("type", new StringSchema())
                    .addProperty("title", new StringSchema())
                    .addProperty("status", new IntegerSchema())
                    .addProperty("detail", new StringSchema())
                    .addProperty("instance", new StringSchema())
                    .addProperty("code", new StringSchema())
                    .addProperty("timestamp", new StringSchema())
                    .addProperty("traceId", new StringSchema())
                    .addProperty(
                            "errors",
                            new ArraySchema().items(new Schema<>().$ref("#/components/schemas/" + VALIDATION_ERROR_SCHEMA_NAME))
                    );
            components.addSchemas(PROBLEM_SCHEMA_NAME, problemSchema);
        }
    }

    private void ensureCommonProblemResponses(OpenAPI openApi) {
        Components components = getOrCreateComponents(openApi);
        for (String code : ERROR_CODES) {
            String responseName = PROBLEM_RESPONSE_PREFIX + code;
            ApiResponse response = new ApiResponse()
                    .description(defaultDescription(code))
                    .content(new Content().addMediaType(
                            "application/problem+json",
                            new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF))
                    ));
            components.addResponses(responseName, response);
        }
    }

    private void normalizeOperations(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        for (PathItem pathItem : openApi.getPaths().values()) {
            for (var entry : pathItem.readOperationsMap().entrySet()) {
                ApiResponses responses = entry.getValue().getResponses();
                if (responses == null) {
                    continue;
                }

                ApiResponse created = responses.get("201");
                if (created != null && created.get$ref() == null) {
                    if (created.getHeaders() == null) {
                        created.setHeaders(new TreeMap<>());
                    }
                    created.getHeaders().putIfAbsent(
                            "Location",
                            new Header().description("URI of created resource").schema(new StringSchema())
                    );
                }

                for (String code : ERROR_CODES) {
                    if (responses.containsKey(code)) {
                        responses.put(code, new ApiResponse().$ref("#/components/responses/" + PROBLEM_RESPONSE_PREFIX + code));
                    }
                }

                List<String> tags = entry.getValue().getTags();
                if (tags != null) {
                    tags.sort(String::compareToIgnoreCase);
                }
            }
        }
    }

    private void sortOpenApi(OpenAPI openApi) {
        if (openApi.getTags() != null) {
            openApi.getTags().sort(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER));
        }

        if (openApi.getPaths() == null) {
            return;
        }

        Paths sortedPaths = new Paths();
        openApi.getPaths().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sortedPaths.addPathItem(entry.getKey(), sortedPathItem(entry.getValue())));
        openApi.setPaths(sortedPaths);
    }

    private PathItem sortedPathItem(PathItem source) {
        PathItem sorted = new PathItem();
        sorted.setSummary(source.getSummary());
        sorted.setDescription(source.getDescription());
        sorted.setServers(source.getServers());
        sorted.setParameters(source.getParameters());
        sorted.set$ref(source.get$ref());
        sorted.setExtensions(source.getExtensions());

        source.readOperationsMap().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::name)))
                .forEach(entry -> sorted.operation(entry.getKey(), entry.getValue()));

        return sorted;
    }

    private Components getOrCreateComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        if (openApi.getComponents().getSchemas() == null) {
            openApi.getComponents().setSchemas(new TreeMap<>());
        }
        if (openApi.getComponents().getResponses() == null) {
            openApi.getComponents().setResponses(new TreeMap<>());
        }
        return openApi.getComponents();
    }

    private String defaultDescription(String code) {
        return switch (code) {
            case "400" -> "Bad Request";
            case "401" -> "Unauthorized";
            case "403" -> "Forbidden";
            case "404" -> "Not Found";
            case "409" -> "Conflict";
            case "500" -> "Internal Server Error";
            case "503" -> "Service Unavailable";
            default -> "Error";
        };
    }
}

