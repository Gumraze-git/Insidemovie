from __future__ import annotations

from fastapi import APIRouter

from api.v1.schemas import HealthResponse
from config import settings

router = APIRouter(prefix="/api/v1", tags=["Health"])


@router.get(
    "/health",
    response_model=HealthResponse,
    status_code=200,
    summary="Health check",
    operation_id="getHealth",
)
async def get_health() -> HealthResponse:
    return HealthResponse(status="ok", service=settings.title)
