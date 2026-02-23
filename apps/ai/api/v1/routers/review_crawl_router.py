from __future__ import annotations

from typing import TYPE_CHECKING

from fastapi import APIRouter, Depends, Path

from api.v1.dependencies import get_review_crawl_service
from api.v1.routers.common import problem_responses
from api.v1.schemas import ReviewCrawlRequest, ReviewCrawlResponse

if TYPE_CHECKING:
    from application.services.review_crawl_service import ReviewCrawlService

router = APIRouter(prefix="/api/v1", tags=["Review Crawl"])


@router.post(
    "/movies/{movieId}/review-crawls",
    response_model=ReviewCrawlResponse,
    status_code=200,
    summary="Crawl reviews for a movie",
    operation_id="createReviewCrawl",
    responses=problem_responses(400, 404, 422, 500, 503),
)
async def create_review_crawl(
    payload: ReviewCrawlRequest,
    movie_id: int = Path(..., alias="movieId", ge=1),
    service: ReviewCrawlService = Depends(get_review_crawl_service),
) -> ReviewCrawlResponse:
    return service.crawl(movie_id, payload)
