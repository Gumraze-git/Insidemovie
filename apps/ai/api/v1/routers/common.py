from __future__ import annotations

from copy import deepcopy


PROBLEM_DESCRIPTIONS: dict[int, str] = {
    400: "Bad Request",
    404: "Not Found",
    422: "Unprocessable Entity",
    500: "Internal Server Error",
    503: "Service Unavailable",
}


def problem_responses(*statuses: int) -> dict[int, dict]:
    response_map: dict[int, dict] = {}
    for status in statuses:
        description = PROBLEM_DESCRIPTIONS.get(status, "Error")
        response_map[status] = {
            "description": description,
            "content": {
                "application/problem+json": {
                    "schema": {"$ref": "#/components/schemas/ProblemDetailContract"}
                }
            },
        }
    return deepcopy(response_map)
