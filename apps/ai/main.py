import uvicorn
from fastapi import FastAPI

from config import settings

from routers.home import router as home_router
from routers.predict import router as predict_router
from routers.recommend import router as recommend_router

app = FastAPI(title=settings.title)

# 라우터 등록
app.include_router(home_router)
app.include_router(predict_router)
app.include_router(recommend_router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)