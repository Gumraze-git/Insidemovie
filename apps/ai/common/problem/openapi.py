from __future__ import annotations

from fastapi import FastAPI
from fastapi.openapi.utils import get_openapi

from common.problem.models import (
    ErrorListContract,
    ProblemDetailContract,
    ValidationErrorItemContract,
)


PROBLEM_RESPONSE_COMPONENTS: dict[str, tuple[str, str]] = {
    "400": ("BadRequestProblem", "Bad Request"),
    "404": ("NotFoundProblem", "Not Found"),
    "422": ("UnprocessableEntityProblem", "Unprocessable Entity"),
    "500": ("InternalServerProblem", "Internal Server Error"),
    "503": ("ServiceUnavailableProblem", "Service Unavailable"),
}


def configure_openapi(app: FastAPI) -> None:
    def custom_openapi() -> dict:
        if app.openapi_schema:
            return app.openapi_schema

        openapi_schema = get_openapi(
            title=app.title,
            version=app.version,
            description=app.description,
            routes=app.routes,
        )

        components = openapi_schema.setdefault("components", {})
        schemas = components.setdefault("schemas", {})
        responses = components.setdefault("responses", {})

        # Ensure shared schema components exist with stable names.
        schemas.setdefault(
            "ValidationErrorItemContract",
            ValidationErrorItemContract.model_json_schema(ref_template="#/components/schemas/{model}"),
        )
        schemas.setdefault(
            "ProblemDetailContract",
            ProblemDetailContract.model_json_schema(ref_template="#/components/schemas/{model}"),
        )
        schemas.setdefault(
            "ErrorListContract",
            ErrorListContract.model_json_schema(ref_template="#/components/schemas/{model}"),
        )

        for status, (name, description) in PROBLEM_RESPONSE_COMPONENTS.items():
            responses[name] = {
                "description": description,
                "content": {
                    "application/problem+json": {
                        "schema": {"$ref": "#/components/schemas/ProblemDetailContract"}
                    }
                },
            }

        for path_item in openapi_schema.get("paths", {}).values():
            for method, operation in path_item.items():
                if method not in {"get", "post", "put", "patch", "delete", "head", "options"}:
                    continue

                operation_responses = operation.setdefault("responses", {})
                for status, (component_name, _) in PROBLEM_RESPONSE_COMPONENTS.items():
                    operation_responses.setdefault(
                        status,
                        {"$ref": f"#/components/responses/{component_name}"},
                    )

        app.openapi_schema = openapi_schema
        return openapi_schema

    app.openapi = custom_openapi
