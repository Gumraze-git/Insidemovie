from __future__ import annotations

import requests

from common.errors import ExternalProviderException


class KinoLightsClient:
    def __init__(self, endpoint: str = "https://gateway.kinolights.com/graphql", timeout: int = 10) -> None:
        self._endpoint = endpoint
        self._timeout = timeout

    def fetch_reviews(self, movie_id: int, review_count: int) -> list[dict[str, str]]:
        headers = {
            "User-Agent": "Mozilla/5.0",
            "Content-Type": "application/json",
        }
        payload = {
            "operationName": "QueryContentReviews",
            "variables": {
                "contentId": movie_id,
                "reviewsOffset": 0,
                "reviewsLimit": review_count,
                "reviewsOrderBy": "LIKE",
                "reviewsOrderOption": "DESC",
            },
            "query": """
            query QueryContentReviews(
              $contentId: Int!,
              $reviewsOffset: Int = 0,
              $reviewsLimit: Int = 10,
              $reviewsOrderBy: ReviewMoviesOrderType!,
              $reviewsOrderOption: OrderOptionType!,
              $reviewType: ReviewFilterType,
            ) {
              reviews(
                movieId: $contentId
                offset: $reviewsOffset
                limit: $reviewsLimit
                orderBy: $reviewsOrderBy
                orderOption: $reviewsOrderOption
                reviewType: $reviewType
              ) {
                reviewTitle
                review
              }
            }
            """,
        }

        try:
            response = requests.post(
                self._endpoint,
                headers=headers,
                json=payload,
                timeout=self._timeout,
            )
            response.raise_for_status()
        except requests.exceptions.RequestException as exc:
            raise ExternalProviderException("Failed to request KinoLights") from exc

        try:
            body = response.json()
        except ValueError as exc:
            raise ExternalProviderException("KinoLights response is not valid JSON") from exc

        if body.get("errors"):
            raise ExternalProviderException("KinoLights returned an error response")

        reviews = body.get("data", {}).get("reviews")
        if reviews is None:
            raise ExternalProviderException("KinoLights response does not include reviews")

        normalized_reviews: list[dict[str, str]] = []
        for review in reviews:
            normalized_reviews.append(
                {
                    "title": str(review.get("reviewTitle", "")),
                    "content": str(review.get("review", "")),
                }
            )

        return normalized_reviews
