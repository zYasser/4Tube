package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Specialized error response that extends ApiResponse with additional error-specific fields.
 * Provides detailed error information including validation errors, error codes, and additional context.
 *
 * @param <T> the type of additional error data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ErrorResponse<T> extends ApiResponse<T> {

    /**
     * Error code identifier for categorizing the error
     */
    private String errorCode;

    /**
     * Detailed error information or feedback data
     */
    private T details;

    /**
     * List of field-level validation errors for form submissions
     */
    private List<ValidationError> validationErrors;

    /**
     * HTTP request path where the error occurred
     */
    private String path;

    /**
     * Additional error context as key-value pairs
     */
    private Map<String, Object> context;

    /**
     * Creates a basic error response with message and error code
     *
     * @param message error message
     * @param errorCode error code identifier
     * @param <T> the type of details data
     * @return ErrorResponse with error status, message, and error code
     */
    public static <T> ErrorResponse<T> of(String message, String errorCode) {
        ErrorResponse<T> response = new ErrorResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response with message, error code, and additional details
     *
     * @param message error message
     * @param errorCode error code identifier
     * @param details additional error details
     * @param <T> the type of details data
     * @return ErrorResponse with error status, message, error code, and details
     */
    public static <T> ErrorResponse<T> of(String message, String errorCode, T details) {
        ErrorResponse<T> response = new ErrorResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setDetails(details);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response with validation errors
     *
     * @param message error message
     * @param validationErrors list of field validation errors
     * @param <T> the type of details data
     * @return ErrorResponse with validation errors
     */
    public static <T> ErrorResponse<T> validationError(String message, List<ValidationError> validationErrors) {
        ErrorResponse<T> response = new ErrorResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode("VALIDATION_ERROR");
        response.setValidationErrors(validationErrors);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Creates an error response with message, error code, path, and context
     *
     * @param message error message
     * @param errorCode error code identifier
     * @param path request path where error occurred
     * @param context additional error context
     * @param <T> the type of details data
     * @return ErrorResponse with comprehensive error information
     */
    public static <T> ErrorResponse<T> withContext(String message, String errorCode, String path, Map<String, Object> context) {
        ErrorResponse<T> response = new ErrorResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setPath(path);
        response.setContext(context);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    /**
     * Inner class representing a field validation error
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * Name of the field that failed validation
         */
        private String field;

        /**
         * Value that was rejected
         */
        private Object rejectedValue;

        /**
         * Validation error message
         */
        private String message;

        /**
         * Validation constraint that was violated
         */
        private String constraint;
    }
}
