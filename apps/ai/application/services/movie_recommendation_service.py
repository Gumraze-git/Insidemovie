from __future__ import annotations

import faiss
import numpy as np
from sqlalchemy.orm import Session

from api.v1.schemas import (
    MovieRecommendationItem,
    MovieRecommendationRequest,
    MovieRecommendationResponse,
)
from common.errors import BadRequestException, NotFoundException
from infrastructure.repositories.movie_emotion_summary_repository import (
    MovieEmotionSummaryRepository,
)


class MovieRecommendationService:
    def __init__(self, repository: MovieEmotionSummaryRepository) -> None:
        self._repository = repository

    def recommend(
        self,
        payload: MovieRecommendationRequest,
        db: Session,
    ) -> MovieRecommendationResponse:
        movie_summaries = self._repository.find_all(db)
        if not movie_summaries:
            raise NotFoundException(
                "NOT_FOUND_MOVIE_EMOTION_SUMMARY",
                "No movie emotion summary data found",
            )

        movie_ids = np.array([int(movie.movie_id) for movie in movie_summaries], dtype="int32")
        movie_vectors = np.array(
            [
                [
                    float(movie.joy or 0.0),
                    float(movie.sadness or 0.0),
                    float(movie.anger or 0.0),
                    float(movie.fear or 0.0),
                    float(movie.disgust or 0.0),
                ]
                for movie in movie_summaries
            ],
            dtype="float32",
        )

        if movie_vectors.size == 0:
            raise NotFoundException(
                "NOT_FOUND_MOVIE_EMOTION_SUMMARY",
                "No movie vectors available for recommendation",
            )

        user_vector = np.array(
            [[payload.joy, payload.sadness, payload.anger, payload.fear, payload.disgust]],
            dtype="float32",
        )

        if movie_vectors.shape[1] != user_vector.shape[1]:
            raise BadRequestException(
                "INVALID_EMOTION_VECTOR",
                "Emotion vector shape is invalid",
            )

        faiss.normalize_L2(movie_vectors)
        faiss.normalize_L2(user_vector)

        index = faiss.IndexFlatIP(movie_vectors.shape[1])
        index.add(movie_vectors)

        limit = min(payload.limit, len(movie_ids))
        distances, indices = index.search(user_vector, limit)

        items = [
            MovieRecommendationItem(
                movieId=int(movie_ids[index_position]),
                similarity=float(score),
            )
            for score, index_position in zip(distances[0], indices[0])
            if index_position >= 0
        ]

        return MovieRecommendationResponse(
            count=len(items),
            items=items,
        )
