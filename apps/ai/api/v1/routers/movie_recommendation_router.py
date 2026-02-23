from __future__ import annotations

from typing import TYPE_CHECKING

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from api.v1.dependencies import get_movie_recommendation_service
from api.v1.routers.common import problem_responses
from api.v1.schemas import MovieRecommendationRequest, MovieRecommendationResponse
from database import get_db

if TYPE_CHECKING:
    from application.services.movie_recommendation_service import MovieRecommendationService

router = APIRouter(prefix="/api/v1", tags=["Movie Recommendation"])


@router.post(
    "/movie-recommendations",
    response_model=MovieRecommendationResponse,
    status_code=200,
    summary="Recommend movies from emotion vector",
    operation_id="createMovieRecommendation",
    responses=problem_responses(400, 404, 422, 500, 503),
)
async def create_movie_recommendation(
    payload: MovieRecommendationRequest,
    db: Session = Depends(get_db),
    service: MovieRecommendationService = Depends(get_movie_recommendation_service),
) -> MovieRecommendationResponse:
    return service.recommend(payload, db)
