from __future__ import annotations

from typing import Any, Optional

from pydantic import BaseModel, Field


class ValidationErrorItemContract(BaseModel):
    field: str
    reason: str
    rejectedValue: Optional[Any] = None


class ProblemDetailContract(BaseModel):
    type: str = "about:blank"
    title: str
    status: int
    detail: str
    instance: str
    code: str
    timestamp: str
    traceId: str
    errors: list[ValidationErrorItemContract] = Field(default_factory=list)


class ErrorListContract(BaseModel):
    errors: list[ValidationErrorItemContract] = Field(default_factory=list)
