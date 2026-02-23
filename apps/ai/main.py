import uvicorn
from fastapi import FastAPI

from api.v1.routers.emotion_prediction_router import router as emotion_prediction_router
from api.v1.routers.health_router import router as health_router
from api.v1.routers.movie_recommendation_router import router as movie_recommendation_router
from api.v1.routers.review_crawl_router import router as review_crawl_router
from common.middleware.trace_id import RequestTraceIdMiddleware
from common.problem.exception_handlers import register_exception_handlers
from common.problem.openapi import configure_openapi
from config import settings


def create_app() -> FastAPI:
    app = FastAPI(
        title=settings.title,
        version="1.0.0",
        description="InsideMovie AI FastAPI v1",
    )

    app.add_middleware(RequestTraceIdMiddleware)

    app.include_router(health_router)
    app.include_router(emotion_prediction_router)
    app.include_router(movie_recommendation_router)
    app.include_router(review_crawl_router)

    register_exception_handlers(app)
    configure_openapi(app)

    return app


app = create_app()


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
