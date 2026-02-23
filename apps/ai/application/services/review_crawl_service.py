from __future__ import annotations

from api.v1.schemas import (
    CrawledReviewItem,
    ReviewCrawlRequest,
    ReviewCrawlResponse,
)
from infrastructure.clients.kinolights_client import KinoLightsClient


class ReviewCrawlService:
    def __init__(self, kinolights_client: KinoLightsClient) -> None:
        self._kinolights_client = kinolights_client

    def crawl(self, movie_id: int, payload: ReviewCrawlRequest) -> ReviewCrawlResponse:
        reviews = self._kinolights_client.fetch_reviews(movie_id, payload.reviewCount)

        normalized_reviews = [
            CrawledReviewItem(title=review["title"], content=review["content"])
            for review in reviews
        ]

        return ReviewCrawlResponse(
            movieId=movie_id,
            requestedCount=payload.reviewCount,
            collectedCount=len(normalized_reviews),
            reviews=normalized_reviews,
        )
