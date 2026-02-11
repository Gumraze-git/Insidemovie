from datetime import datetime
from typing import Dict, Optional

from pydantic import BaseModel


class TextItem(BaseModel):
    text: str

class Prediction(BaseModel):
    text: str
    probabilities: Dict[str, float]
    timestamp: Optional[datetime] = None