package com.insidemovie.backend.common.problem;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemDetailResponseWriter {
    private final ProblemDetailFactory problemDetailFactory;
    private final ObjectMapper objectMapper;

    public void write(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String detail,
            jakarta.servlet.http.HttpServletRequest request
    ) throws IOException {
        ProblemDetail pd = problemDetailFactory.create(status, code, detail, request);
        Object traceId = pd.getProperties().get("traceId");
        if (traceId != null) {
            response.setHeader(RequestTraceIdFilter.TRACE_ID_HEADER, traceId.toString());
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), pd);
    }
}
