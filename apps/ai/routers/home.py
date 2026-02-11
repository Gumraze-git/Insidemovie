from fastapi import APIRouter

from config import settings

router = APIRouter()

@router.get("/", summary="홈")
async def read_home():
    return {"message": settings.title}