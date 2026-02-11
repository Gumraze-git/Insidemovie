import os
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

from fastapi import FastAPI, APIRouter, Depends
from pydantic import BaseModel
import faiss
from typing import Dict
from database import get_db
from models import MovieEmotionSummary
from sqlalchemy.orm import Session
import numpy as np


router = APIRouter(
    prefix="/recommend", tags=["Recommend"]
)

# 감정 요청
class EmotionRequestDTO(BaseModel):
    joy: float
    anger: float
    fear: float
    disgust: float
    sadness: float

@router.post("/emotion")
async def recommend_movie(emotion: EmotionRequestDTO, db: Session = Depends(get_db)):
    user_vector = np.array([[
        emotion.joy,
        emotion.sadness,
        emotion.anger,
        emotion.fear,
        emotion.disgust
    ]], dtype='float32')


    movies = db.query(MovieEmotionSummary).all()
    movie_vector = {
        movie.movie_id :
        [
            movie.joy,
            movie.sadness,
            movie.anger,
            movie.fear,
            movie.disgust
        ]
        for movie in movies
    }

    movie_ids = np.array(list(movie_vector.keys())).astype('int')
    movie_vector = np.array(list(movie_vector.values())).astype("float32")
        
    # 벡터 정규화(코사인 유사도)
    faiss.normalize_L2(movie_vector)
    faiss.normalize_L2(user_vector)

    # Faiss 인덱스 생성(내적 기반)
    index = faiss.IndexFlatIP(movie_vector.shape[1])
    index.add(movie_vector)


    distances, indices = index.search(user_vector, 10)

    results = []
    for score, idx in zip(distances[0], indices[0]):
        results.append({
            "movie_id" : int(movie_ids[idx]),
            "similarity": float(score)
        })

    return results