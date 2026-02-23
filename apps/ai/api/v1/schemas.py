from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str
    service: str


class EmotionPredictionRequest(BaseModel):
    text: str = Field(min_length=1)
    aggregation: Literal["overall_avg", "split_avg", "full"] = "overall_avg"


class EmotionPredictionResponse(BaseModel):
    text: str
    aggregation: Literal["overall_avg", "split_avg", "full"]
    probabilities: dict[str, float]
    analyzedAt: datetime


class MovieRecommendationRequest(BaseModel):
    joy: float = Field(ge=0)
    sadness: float = Field(ge=0)
    anger: float = Field(ge=0)
    fear: float = Field(ge=0)
    disgust: float = Field(ge=0)
    limit: int = Field(default=10, ge=1, le=50)


class MovieRecommendationItem(BaseModel):
    movieId: int
    similarity: float


class MovieRecommendationResponse(BaseModel):
    count: int
    items: list[MovieRecommendationItem]


class ReviewCrawlRequest(BaseModel):
    reviewCount: int = Field(default=100, ge=1, le=500)


class CrawledReviewItem(BaseModel):
    title: str
    content: str


class ReviewCrawlResponse(BaseModel):
    movieId: int
    requestedCount: int
    collectedCount: int
    reviews: list[CrawledReviewItem]
