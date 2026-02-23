from __future__ import annotations

from typing import Any, Optional


class DomainException(Exception):
    def __init__(
        self,
        status_code: int,
        code: str,
        detail: str,
        title: str,
        errors: Optional[list[dict[str, Any]]] = None,
    ) -> None:
        super().__init__(detail)
        self.status_code = status_code
        self.code = code
        self.detail = detail
        self.title = title
        self.errors = errors or []


class BadRequestException(DomainException):
    def __init__(
        self,
        code: str,
        detail: str,
        errors: Optional[list[dict[str, Any]]] = None,
    ) -> None:
        super().__init__(400, code, detail, "Bad Request", errors)


class NotFoundException(DomainException):
    def __init__(self, code: str, detail: str) -> None:
        super().__init__(404, code, detail, "Not Found")


class ConflictException(DomainException):
    def __init__(self, code: str, detail: str) -> None:
        super().__init__(409, code, detail, "Conflict")


class ExternalProviderException(DomainException):
    def __init__(self, detail: str) -> None:
        super().__init__(503, "EXTERNAL_PROVIDER_ERROR", detail, "Service Unavailable")


class DatabaseUnavailableException(DomainException):
    def __init__(self, detail: str) -> None:
        super().__init__(503, "DATABASE_UNAVAILABLE", detail, "Service Unavailable")
