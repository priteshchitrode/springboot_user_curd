package com.javatechie.crud.example.user;

import com.javatechie.crud.example.response.ApiResponse;
import com.javatechie.crud.example.response.ErrorType;
import com.javatechie.crud.example.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * User Controller - Clean HTTP routing
 * Delegates all business logic to UserService
 * Returns Result<T> and converts to ResponseEntity
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get user profile by ID
     * Returns: Success -> User data
     *          Error   -> Validation or not found error
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<User>> getProfile(@PathVariable String id) {
        Result<User> result = userService.getProfile(id);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(result.getOrNull(), "Profile fetched successfully")
            );
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    /**
     * Update user profile
     * Returns: Success -> Updated user data
     *          Error   -> Validation, authorization, or not found error
     */
    @PostMapping("/updateProfile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody User updatedUser) {
        // Get current user ID from JWT token
        Long currentUserId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Invalid or expired token"));
        }

        Result<User> result = userService.updateProfile(updatedUser, currentUserId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(result.getOrNull(), "Profile updated successfully")
            );
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    /**
     * Get all users
     * Returns: Success -> List of all users
     *          Error   -> No users found or server error
     */
    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        Result<List<User>> result = userService.getAllUsers();

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(result.getOrNull(), "Users fetched successfully")
            );
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    /**
     * Delete user account
     * Returns: Success -> Confirmation message
     *          Error   -> Authorization, not found, or server error
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        // Get current user ID from JWT token
        Long currentUserId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: Invalid or expired token"));
        }

        Result<Void> result = userService.deleteUser(userId, currentUserId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(null, "User deleted successfully")
            );
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    /**
     * Get user by email
     * Returns: Success -> User data
     *          Error   -> Not found or server error
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        Result<User> result = userService.getUserByEmail(email);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success(result.getOrNull(), "User retrieved successfully")
            );
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    // ============ Helper Methods ============

    /**
     * Generic error handler
     * Maps ErrorType to appropriate HTTP status and response
     */
    private <T> ResponseEntity<ApiResponse<T>> handleErrorResult(ErrorType error) {
        return ResponseEntity.status(error.getHttpStatus())
                .body(ApiResponse.error(error.getMessage()));
    }
}