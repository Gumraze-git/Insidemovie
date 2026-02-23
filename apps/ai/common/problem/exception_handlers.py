from __future__ import annotations

import logging

import requests
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from sqlalchemy.exc import SQLAlchemyError
from starlette.exceptions import HTTPException as StarletteHTTPException

from common.errors import DomainException
from common.problem.factory import ProblemDetailFactory
from common.problem.models import ProblemDetailContract

logger = logging.getLogger(__name__)


def _problem_response(problem: ProblemDetailContract) -> JSONResponse:
    headers = {"X-Trace-Id": problem.traceId} if problem.traceId else None
    return JSONResponse(
        status_code=problem.status,
        content=problem.model_dump(),
        media_type="application/problem+json",
        headers=headers,
    )


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(RequestValidationError)
    async def request_validation_error_handler(
        request: Request,
        exc: RequestValidationError,
    ) -> JSONResponse:
        errors: list[dict[str, object]] = []
        for error in exc.errors():
            loc = list(error.get("loc", []))
            if loc and loc[0] in {"body", "query", "path", "header", "cookie"}:
                field = ".".join(str(part) for part in loc[1:]) or str(loc[0])
            else:
                field = ".".join(str(part) for part in loc) or "request"
            errors.append(
                {
                    "field": field,
                    "reason": error.get("msg", "invalid value"),
                    "rejectedValue": None,
                }
            )

        problem = ProblemDetailFactory.create(
            request,
            status=400,
            title="Bad Request",
            detail="Validation failed",
            code="VALIDATION_ERROR",
            errors=errors,
        )
        return _problem_response(problem)

    @app.exception_handler(DomainException)
    async def domain_exception_handler(request: Request, exc: DomainException) -> JSONResponse:
        problem = ProblemDetailFactory.from_domain_exception(request, exc)
        return _problem_response(problem)

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(request: Request, exc: StarletteHTTPException) -> JSONResponse:
        problem = ProblemDetailFactory.from_http_exception(
            request,
            status_code=exc.status_code,
            detail=str(exc.detail),
        )
        return _problem_response(problem)

    @app.exception_handler(SQLAlchemyError)
    async def sqlalchemy_exception_handler(request: Request, exc: SQLAlchemyError) -> JSONResponse:
        logger.exception("database error", exc_info=exc)
        problem = ProblemDetailFactory.create(
            request,
            status=503,
            title="Service Unavailable",
            detail="Database connection is unavailable",
            code="DATABASE_UNAVAILABLE",
        )
        return _problem_response(problem)

    @app.exception_handler(requests.exceptions.RequestException)
    async def requests_exception_handler(request: Request, exc: requests.exceptions.RequestException) -> JSONResponse:
        logger.exception("external provider error", exc_info=exc)
        problem = ProblemDetailFactory.create(
            request,
            status=503,
            title="Service Unavailable",
            detail="External provider request failed",
            code="EXTERNAL_PROVIDER_ERROR",
        )
        return _problem_response(problem)

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("unhandled error", exc_info=exc)
        problem = ProblemDetailFactory.create(
            request,
            status=500,
            title="Internal Server Error",
            detail="An unexpected error occurred",
            code="INTERNAL_SERVER_ERROR",
        )
        return _problem_response(problem)
