import os

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    collection_name: str = "predictions"
    model_dir: str = os.getenv("MODEL_DIR", "models/0717_kobert_5_emotion_model")
    title: str = "MovieMood - KoBERT Emotion API"

    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()