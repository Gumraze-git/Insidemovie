from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

from api.v1.dependencies import (
    get_emotion_prediction_service,
    get_movie_recommendation_service,
    get_review_crawl_service,
)
from common.errors import ExternalProviderException
from database import get_db
from main import app


class FakeEmotionPredictionService:
    def predict(self, payload):
        return {
            "text": payload.text,
            "aggregation": payload.aggregation,
            "probabilities": {
                "joy": 55.0,
                "sadness": 15.0,
                "anger": 10.0,
                "fear": 10.0,
                "disgust": 10.0,
            },
            "analyzedAt": "2026-02-23T00:00:00Z",
        }


class FakeMovieRecommendationService:
    def recommend(self, payload, db):
        return {
            "count": 2,
            "items": [
                {"movieId": 101, "similarity": 0.91},
                {"movieId": 102, "similarity": 0.88},
            ],
        }


class FakeReviewCrawlService:
    def crawl(self, movie_id: int, payload):
        return {
            "movieId": movie_id,
            "requestedCount": payload.reviewCount,
            "collectedCount": 1,
            "reviews": [
                {
                    "title": "재밌어요",
                    "content": "추천합니다",
                }
            ],
        }


@pytest.fixture
def client() -> TestClient:
    app.dependency_overrides[get_emotion_prediction_service] = lambda: FakeEmotionPredictionService()
    app.dependency_overrides[get_movie_recommendation_service] = lambda: FakeMovieRecommendationService()
    app.dependency_overrides[get_review_crawl_service] = lambda: FakeReviewCrawlService()

    def override_get_db():
        yield object()

    app.dependency_overrides[get_db] = override_get_db

    with TestClient(app) as test_client:
        yield test_client

    app.dependency_overrides.clear()


def test_emotion_prediction_contract(client: TestClient) -> None:
    response = client.post(
        "/api/v1/emotion-predictions",
        json={"text": "영화가 매우 좋았어요", "aggregation": "overall_avg"},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["text"] == "영화가 매우 좋았어요"
    assert body["aggregation"] == "overall_avg"
    assert "probabilities" in body
    assert "analyzedAt" in body


def test_movie_recommendation_contract(client: TestClient) -> None:
    response = client.post(
        "/api/v1/movie-recommendations",
        json={
            "joy": 30,
            "sadness": 20,
            "anger": 10,
            "fear": 5,
            "disgust": 8,
            "limit": 2,
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["count"] == 2
    assert body["items"][0]["movieId"] == 101


def test_review_crawl_contract(client: TestClient) -> None:
    response = client.post(
        "/api/v1/movies/12/review-crawls",
        json={"reviewCount": 50},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["movieId"] == 12
    assert body["requestedCount"] == 50
    assert body["collectedCount"] == 1


def test_validation_error_returns_problem_detail_with_400(client: TestClient) -> None:
    response = client.post(
        "/api/v1/emotion-predictions",
        json={"aggregation": "overall_avg"},
        headers={"X-Trace-Id": "trace-test-001"},
    )

    assert response.status_code == 400
    assert response.headers["content-type"].startswith("application/problem+json")
    body = response.json()
    assert body["code"] == "VALIDATION_ERROR"
    assert body["traceId"] == "trace-test-001"
    assert len(body["errors"]) > 0
    assert "field" in body["errors"][0]


def test_unknown_path_returns_problem_detail(client: TestClient) -> None:
    response = client.post("/predict/overall_avg", json={"text": "legacy"})

    assert response.status_code == 404
    assert response.headers["content-type"].startswith("application/problem+json")
    assert response.json()["code"] == "NOT_FOUND"


def test_external_provider_error_returns_problem_detail(client: TestClient) -> None:
    class FailingReviewCrawlService:
        def crawl(self, movie_id: int, payload):
            raise ExternalProviderException("failed to request provider")

    app.dependency_overrides[get_review_crawl_service] = lambda: FailingReviewCrawlService()

    response = client.post(
        "/api/v1/movies/99/review-crawls",
        json={"reviewCount": 10},
    )

    assert response.status_code == 503
    assert response.headers["content-type"].startswith("application/problem+json")
    assert response.json()["code"] == "EXTERNAL_PROVIDER_ERROR"
