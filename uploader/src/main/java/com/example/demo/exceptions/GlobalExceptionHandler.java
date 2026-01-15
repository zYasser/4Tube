package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.demo.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ErrorResponse<Void>> handleHttpException(HttpException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ErrorResponse.of(ex.getMessage(), ex.getErrorCode().toString()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Resource not found", "NOT_FOUND"));
    }

    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ErrorResponse<Void>> handleException(Exception ex) {
    //     // Check if this is a static resource not found error
    //     if (ex.getMessage() != null && ex.getMessage().contains("No static resource")) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND)
    //                 .body(ErrorResponse.of("Resource not found", "NOT_FOUND"));
    //     }
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //             .body(ErrorResponse.of(ex.getMessage(), "INTERNAL_SERVER_ERROR"));
    // }
}
