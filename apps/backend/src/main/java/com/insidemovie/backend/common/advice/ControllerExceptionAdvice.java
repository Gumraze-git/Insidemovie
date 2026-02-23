package com.insidemovie.backend.common.advice;

import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.problem.ProblemDetailFactory;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionAdvice extends ResponseEntityExceptionHandler {
    private final ProblemDetailFactory problemDetailFactory;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(BaseException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        String code = ErrorStatus.fromMessage(ex.getResponseMessage())
                .map(ErrorStatus::getCode)
                .orElseGet(() -> ex.getErrorCode() == null ? "UNKNOWN_ERROR" : ex.getErrorCode());

        ProblemDetail pd = problemDetailFactory.create(status, code, ex.getResponseMessage(), request);
        return ResponseEntity.status(status).body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.BAD_REQUEST,
                ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getCode(),
                ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getMessage(),
                servletRequest
        );
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.BAD_REQUEST,
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                request
        );
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.UNAUTHORIZED,
                "BAD_CREDENTIALS",
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandled(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        FieldError fieldError = Objects.requireNonNull(ex.getFieldError());
        List<Map<String, Object>> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, Object> item = new HashMap<>();
            item.put("field", error.getField());
            item.put("reason", error.getDefaultMessage());
            item.put("rejectedValue", error.getRejectedValue());
            errors.add(item);
        }

        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.BAD_REQUEST,
                ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getCode(),
                String.format("%s. (%s)", fieldError.getDefaultMessage(), fieldError.getField()),
                servletRequest,
                errors
        );
        return ResponseEntity.badRequest().body(pd);
    }
}
