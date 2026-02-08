package com.javatechie.crud.example.user;

import com.javatechie.crud.example.user.*;
import com.javatechie.crud.example.response.ErrorType.*;
import com.javatechie.crud.example.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Service Layer
 * Handles all user-related business logic and validation
 * Returns Result<T> for type-safe error handling
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ============ Validation Methods ============

    /**
     * Validate user ID is valid number
     */
    private Result<Long> validateUserId(String idString) {
        try {
            Long userId = Long.parseLong(idString);
            if (userId <= 0) {
                return new Result.Error<>(new ValidationError("userId", "Must be a positive number"));
            }
            return new Result.Success<>(userId);
        } catch (NumberFormatException e) {
            return new Result.Error<>(new ValidationError("userId", "Must be a valid number"));
        }
    }

    /**
     * Validate user exists
     */
    private Result<User> validateUserExists(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new Result.Error<>(new ResourceNotFoundError("User"));
        }
        return new Result.Success<>(user);
    }

    /**
     * Validate authorization - user can only update their own profile
     */
    private Result<Void> validateAuthorization(Long requestedUserId, Long currentUserId) {
        if (!requestedUserId.equals(currentUserId)) {
            return new Result.Error<>(new AccessDeniedError());
        }
        return new Result.Success<>(null);
    }

    /**
     * Validate update request fields
     */
    private Result<Void> validateUpdateRequest(User updatedUser) {
        if (updatedUser == null) {
            return new Result.Error<>(new BadRequestError("User data is required"));
        }

        if (updatedUser.getId() == null || updatedUser.getId() <= 0) {
            return new Result.Error<>(new ValidationError("userId", "Valid user ID is required"));
        }

        return new Result.Success<>(null);
    }

    // ============ User Operations ============

    /**
     * Get user profile by ID
     * Returns: Success -> User data (without password)
     *          Error   -> Not found or validation error
     */
    public Result<User> getProfile(String idString) {
        // Validate ID format
        Result<Long> idValidation = validateUserId(idString);
        if (idValidation.isError()) {
            return new Result.Error<>(idValidation.getErrorOrNull());
        }

        Long userId = idValidation.getOrNull();

        try {
            // Check if user exists
            Result<User> userValidation = validateUserExists(userId);
            if (userValidation.isError()) {
                return new Result.Error<>(userValidation.getErrorOrNull());
            }

            User user = userValidation.getOrNull();
            user.setPassword(null); // Hide password
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }

    /**
     * Update user profile with authorization check
     * Returns: Success -> Updated user data
     *          Error   -> Validation, authorization, or not found error
     */
    public Result<User> updateProfile(User updatedUser, Long currentUserId) {
        // Validate request
        Result<Void> requestValidation = validateUpdateRequest(updatedUser);
        if (requestValidation.isError()) {
            return new Result.Error<>(requestValidation.getErrorOrNull());
        }

        Long userId = updatedUser.getId();

        // Validate authorization
        Result<Void> authValidation = validateAuthorization(userId, currentUserId);
        if (authValidation.isError()) {
            return new Result.Error<>(authValidation.getErrorOrNull());
        }

        try {
            // Check if user exists
            Result<User> userValidation = validateUserExists(userId);
            if (userValidation.isError()) {
                return new Result.Error<>(userValidation.getErrorOrNull());
            }

            User existingUser = userValidation.getOrNull();

            // Update fields if provided
            if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().trim().isEmpty()) {
                existingUser.setFirstName(updatedUser.getFirstName());
            }

            if (updatedUser.getLastName() != null && !updatedUser.getLastName().trim().isEmpty()) {
                existingUser.setLastName(updatedUser.getLastName());
            }

            if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().trim().isEmpty()) {
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            }

            if (updatedUser.getAddress() != null && !updatedUser.getAddress().trim().isEmpty()) {
                existingUser.setAddress(updatedUser.getAddress());
            }

            existingUser.setUpdatedAt(LocalDateTime.now());

            // Save and return
            User savedUser = userRepository.save(existingUser);
            savedUser.setPassword(null); // Hide password
            return new Result.Success<>(savedUser);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }

    /**
     * Get all users
     * Returns: Success -> List of all users
     *          Error   -> No users found or server error
     */
    public Result<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return new Result.Error<>(new ResourceNotFoundError("Users"));
            }

            // Hide passwords from all users
            users.forEach(user -> user.setPassword(null));
            return new Result.Success<>(users);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }

    /**
     * Delete user with authorization check
     * Returns: Success -> Void
     *          Error   -> Authorization, not found, or server error
     */
    public Result<Void> deleteUser(Long userId, Long currentUserId) {
        // Validate authorization
        Result<Void> authValidation = validateAuthorization(userId, currentUserId);
        if (authValidation.isError()) {
            return new Result.Error<>(authValidation.getErrorOrNull());
        }

        try {
            // Check if user exists
            Result<User> userValidation = validateUserExists(userId);
            if (userValidation.isError()) {
                return new Result.Error<>(userValidation.getErrorOrNull());
            }

            // Delete user
            userRepository.deleteById(userId);
            return new Result.Success<>(null);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }

    /**
     * Search user by email
     * Returns: Success -> User data
     *          Error   -> Not found or server error
     */
    public Result<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Email"));
        }

        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return new Result.Error<>(new ResourceNotFoundError("User with email: " + email));
            }

            user.setPassword(null); // Hide password
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }
}