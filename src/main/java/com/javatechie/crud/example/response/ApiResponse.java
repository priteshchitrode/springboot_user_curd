package com.javatechie.crud.example.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API Response wrapper
 * Returns success or error in a consistent format
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private Long timestamp;

    // Success response
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(
                "Success",
                data,
                message,
                System.currentTimeMillis()
        );
    }

    // Error response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                "Failed",
                null,
                message,
                System.currentTimeMillis()
        );
    }
}