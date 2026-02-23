from __future__ import annotations

from sqlalchemy.orm import Session

from models import MovieEmotionSummary


class MovieEmotionSummaryRepository:
    def find_all(self, db: Session) -> list[MovieEmotionSummary]:
        return db.query(MovieEmotionSummary).all()
