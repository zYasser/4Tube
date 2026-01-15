package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for standardized HTTP responses.
 * Provides consistent structure for all API endpoints with success status,
 * message, data payload, and timestamp information.
 *
 * @param <T> the type of data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /**
     * Indicates whether the operation was successful
     */
    private boolean success;

    /**
     * Human-readable message describing the result
     */
    private String message;

    /**
     * The actual data payload for successful operations
     */
    private T data;

    /**
     * Timestamp when the response was created
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();



    /**
     * Creates a successful response with data and default success message
     *
     * @param data the response data
     * @param <T> the type of data
     * @return ApiResponse with success status and data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with custom message and data
     *
     * @param message custom success message
     * @param data the response data
     * @param <T> the type of data
     * @return ApiResponse with success status, custom message, and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with only a message (no data)
     *
     * @param message success message
     * @param <T> the type of data (can be Void or any type)
     * @return ApiResponse with success status and message
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Creates an error response with message
     *
     * @param message error message
     * @param <T> the type of data (can be Void or any type)
     * @return ApiResponse with error status and message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
