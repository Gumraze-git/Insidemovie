from __future__ import annotations

from datetime import datetime, timezone

from api.v1.schemas import EmotionPredictionRequest, EmotionPredictionResponse
from common.errors import BadRequestException
from infrastructure.ml.kobert_predictor import KoBertPredictor


class EmotionPredictionService:
    def __init__(self, predictor: KoBertPredictor) -> None:
        self._predictor = predictor

    def predict(self, payload: EmotionPredictionRequest) -> EmotionPredictionResponse:
        stripped_text = payload.text.strip()
        if not stripped_text:
            raise BadRequestException("INVALID_TEXT", "text must not be blank")

        probabilities = self._predictor.predict(stripped_text, payload.aggregation)

        return EmotionPredictionResponse(
            text=stripped_text,
            aggregation=payload.aggregation,
            probabilities=probabilities,
            analyzedAt=datetime.now(timezone.utc),
        )
