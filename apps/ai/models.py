from sqlalchemy import Column, Integer, Float, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class MovieEmotionSummary(Base):
    __tablename__ = "movie_emotion_summary"

    movie_id = Column(Integer, primary_key=True, index=True)
    joy = Column(Float)
    anger = Column(Float)
    fear = Column(Float)
    disgust = Column(Float)
    sadness = Column(Float)