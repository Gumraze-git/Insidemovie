package com.insidemovie.backend.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {
    HttpStatus statusCode;
    String responseMessage;
    String errorCode;

    public BaseException(HttpStatus statusCode) {
        super();
        this.statusCode = statusCode;
        this.errorCode = statusCode.name();
    }

    public BaseException(HttpStatus statusCode, String responseMessage) {
        super(responseMessage);
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.errorCode = statusCode.name();
    }

    public BaseException(HttpStatus statusCode, String responseMessage, String errorCode) {
        super(responseMessage);
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return this.statusCode.value();
    }
}
