package com.insidemovie.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BaseException {
    public ExternalServiceException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
