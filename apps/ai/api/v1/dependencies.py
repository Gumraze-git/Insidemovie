from __future__ import annotations

from functools import lru_cache
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from application.services.emotion_prediction_service import EmotionPredictionService
    from application.services.movie_recommendation_service import MovieRecommendationService
    from application.services.review_crawl_service import ReviewCrawlService


@lru_cache
def get_emotion_prediction_service() -> EmotionPredictionService:
    from application.services.emotion_prediction_service import EmotionPredictionService
    from config import settings
    from infrastructure.ml.kobert_predictor import KoBertPredictor

    predictor = KoBertPredictor(settings.model_dir)
    return EmotionPredictionService(predictor)


@lru_cache
def get_movie_recommendation_service() -> MovieRecommendationService:
    from application.services.movie_recommendation_service import MovieRecommendationService
    from infrastructure.repositories.movie_emotion_summary_repository import (
        MovieEmotionSummaryRepository,
    )

    repository = MovieEmotionSummaryRepository()
    return MovieRecommendationService(repository)


@lru_cache
def get_review_crawl_service() -> ReviewCrawlService:
    from application.services.review_crawl_service import ReviewCrawlService
    from infrastructure.clients.kinolights_client import KinoLightsClient

    client = KinoLightsClient()
    return ReviewCrawlService(client)
