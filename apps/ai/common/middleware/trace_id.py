from __future__ import annotations

from uuid import uuid4

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response


class RequestTraceIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next) -> Response:
        trace_id = request.headers.get("X-Trace-Id") or str(uuid4())
        request.state.trace_id = trace_id

        response = await call_next(request)
        response.headers.setdefault("X-Trace-Id", trace_id)
        return response
