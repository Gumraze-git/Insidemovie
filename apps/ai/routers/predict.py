from datetime import datetime

from fastapi import APIRouter, HTTPException

from schemas import TextItem, Prediction
from services.prediction import (
    predict_emotion_overall_avg,
)

router = APIRouter(prefix="/predict", tags=["Prediction"])


@router.post("/overall_avg", response_model=Prediction)
async def predict_overall_avg(item: TextItem):
    try:
        probs = predict_emotion_overall_avg(item.text)
        record = {
            "text": item.text,
            "probabilities": probs,
            "timestamp": datetime.utcnow()
        }
        return record
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))