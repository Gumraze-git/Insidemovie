from __future__ import annotations

from fastapi.testclient import TestClient

from main import app


def test_openapi_has_only_new_paths_and_problem_detail_contract() -> None:
    with TestClient(app) as client:
        response = client.get("/openapi.json")

    assert response.status_code == 200
    schema = response.json()
    paths = schema["paths"]

    assert "/api/v1/health" in paths
    assert "/api/v1/emotion-predictions" in paths
    assert "/api/v1/movie-recommendations" in paths
    assert "/api/v1/movies/{movieId}/review-crawls" in paths

    assert "/" not in paths
    assert "/predict/overall_avg" not in paths
    assert "/recommend/emotion" not in paths
    assert "/review/crawl" not in paths

    components = schema["components"]
    assert "ProblemDetailContract" in components["schemas"]
    assert "ValidationErrorItemContract" in components["schemas"]
    assert "ErrorListContract" in components["schemas"]

    assert "application/problem+json" in components["responses"]["BadRequestProblem"]["content"]


def test_openapi_operation_status_codes() -> None:
    with TestClient(app) as client:
        schema = client.get("/openapi.json").json()

    operations = [
        ("/api/v1/health", "get"),
        ("/api/v1/emotion-predictions", "post"),
        ("/api/v1/movie-recommendations", "post"),
        ("/api/v1/movies/{movieId}/review-crawls", "post"),
    ]

    for path, method in operations:
        responses = schema["paths"][path][method]["responses"]
        assert "200" in responses
        assert "400" in responses
        assert "404" in responses
        assert "500" in responses
        assert "503" in responses
