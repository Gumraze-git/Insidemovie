package com.insidemovie.backend.common.problem;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class ProblemDetailFactory {

    public ProblemDetail create(
            HttpStatus status,
            String code,
            String detail,
            HttpServletRequest request
    ) {
        return create(status, code, detail, request, List.of());
    }

    public ProblemDetail create(
            HttpStatus status,
            String code,
            String detail,
            HttpServletRequest request,
            List<Map<String, Object>> errors
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(buildTypeUri(code));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", code);
        pd.setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
        Object traceId = request.getAttribute(RequestTraceIdFilter.TRACE_ID_ATTR);
        String resolvedTraceId = traceId == null ? UUID.randomUUID().toString() : traceId.toString();
        request.setAttribute(RequestTraceIdFilter.TRACE_ID_ATTR, resolvedTraceId);
        pd.setProperty("traceId", resolvedTraceId);
        if (errors != null && !errors.isEmpty()) {
            pd.setProperty("errors", errors);
        }
        return pd;
    }

    private URI buildTypeUri(String code) {
        String normalized = code == null ? "unknown" : code.toLowerCase(Locale.ROOT).replace('_', '-');
        return URI.create("https://insidemovie.dev/problems/" + normalized);
    }
}
