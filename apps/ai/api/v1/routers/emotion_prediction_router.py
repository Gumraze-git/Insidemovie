from __future__ import annotations

from typing import TYPE_CHECKING

from fastapi import APIRouter, Depends

from api.v1.dependencies import get_emotion_prediction_service
from api.v1.routers.common import problem_responses
from api.v1.schemas import EmotionPredictionRequest, EmotionPredictionResponse

if TYPE_CHECKING:
    from application.services.emotion_prediction_service import EmotionPredictionService

router = APIRouter(prefix="/api/v1", tags=["Emotion Prediction"])


@router.post(
    "/emotion-predictions",
    response_model=EmotionPredictionResponse,
    status_code=200,
    summary="Predict emotions for text",
    operation_id="createEmotionPrediction",
    responses=problem_responses(400, 404, 422, 500, 503),
)
async def create_emotion_prediction(
    payload: EmotionPredictionRequest,
    service: EmotionPredictionService = Depends(get_emotion_prediction_service),
) -> EmotionPredictionResponse:
    return service.predict(payload)
