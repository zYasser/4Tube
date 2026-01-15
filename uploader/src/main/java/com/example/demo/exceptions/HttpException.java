package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class HttpException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String message;
    private final String errorCode;

    public HttpException(HttpStatus statusCode, String message, String errorCode) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorCode = errorCode;
    }

}
