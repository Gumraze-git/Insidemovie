from __future__ import annotations

from datetime import datetime, timezone
from typing import Any, Optional

from fastapi import Request

from common.errors import DomainException
from common.problem.models import ProblemDetailContract, ValidationErrorItemContract


HTTP_STATUS_CODE_MAP: dict[int, tuple[str, str]] = {
    400: ("BAD_REQUEST", "Bad Request"),
    401: ("UNAUTHORIZED", "Unauthorized"),
    403: ("FORBIDDEN", "Forbidden"),
    404: ("NOT_FOUND", "Not Found"),
    405: ("METHOD_NOT_ALLOWED", "Method Not Allowed"),
    409: ("CONFLICT", "Conflict"),
    422: ("UNPROCESSABLE_ENTITY", "Unprocessable Entity"),
    500: ("INTERNAL_SERVER_ERROR", "Internal Server Error"),
    503: ("SERVICE_UNAVAILABLE", "Service Unavailable"),
}


class ProblemDetailFactory:
    @staticmethod
    def _timestamp() -> str:
        return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")

    @staticmethod
    def _trace_id(request: Request) -> str:
        return getattr(request.state, "trace_id", "")

    @classmethod
    def create(
        cls,
        request: Request,
        *,
        status: int,
        title: str,
        detail: str,
        code: str,
        errors: Optional[list[dict[str, Any]]] = None,
        type_uri: str = "about:blank",
    ) -> ProblemDetailContract:
        normalized_errors = [ValidationErrorItemContract(**error) for error in (errors or [])]
        return ProblemDetailContract(
            type=type_uri,
            title=title,
            status=status,
            detail=detail,
            instance=request.url.path,
            code=code,
            timestamp=cls._timestamp(),
            traceId=cls._trace_id(request),
            errors=normalized_errors,
        )

    @classmethod
    def from_domain_exception(cls, request: Request, exc: DomainException) -> ProblemDetailContract:
        return cls.create(
            request,
            status=exc.status_code,
            title=exc.title,
            detail=exc.detail,
            code=exc.code,
            errors=exc.errors,
        )

    @classmethod
    def from_http_exception(
        cls,
        request: Request,
        *,
        status_code: int,
        detail: str,
    ) -> ProblemDetailContract:
        code, title = HTTP_STATUS_CODE_MAP.get(
            status_code,
            ("HTTP_ERROR", "HTTP Error"),
        )
        return cls.create(
            request,
            status=status_code,
            title=title,
            detail=detail,
            code=code,
        )
