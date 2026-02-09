package com.javatechie.crud.example.user;
import com.javatechie.crud.example.response.ErrorType.*;
import com.javatechie.crud.example.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // Validation Methods
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


    private Result<User> validateUserExists(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new Result.Error<>(new ResourceNotFoundError("User"));
        }
        return new Result.Success<>(user);
    }


    private Result<Void> validateAuthorization(Long requestedUserId, Long currentUserId) {
        if (!requestedUserId.equals(currentUserId)) {
            return new Result.Error<>(new AccessDeniedError());
        }
        return new Result.Success<>(null);
    }


    private Result<Void> validateUpdateRequest(User updatedUser) {
        if (updatedUser == null) {
            return new Result.Error<>(new BadRequestError("User data is required"));
        }
        if (updatedUser.getId() == null || updatedUser.getId() <= 0) {
            return new Result.Error<>(new ValidationError("userId", "Valid user ID is required"));
        }
        return new Result.Success<>(null);
    }


    // User Operations
    public Result<User> getProfile(String idString) {
        // Validate ID format
        Result<Long> idValidation = validateUserId(idString);
        if (idValidation.isError()) {
            return new Result.Error<>(idValidation.getErrorOrNull());
        }

        Long userId = idValidation.getValueOrNull();

        try {
            // Check if user exists
            Result<User> userValidation = validateUserExists(userId);
            if (userValidation.isError()) {
                return new Result.Error<>(userValidation.getErrorOrNull());
            }

            User user = userValidation.getValueOrNull();
            user.setPassword(null); // Hide password
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    // Update Profile
    public Result<User> updateProfile(User updatedUser) {

        // Validate request
        Result<Void> requestValidation = validateUpdateRequest(updatedUser);
        if (requestValidation.isError()) {
            return new Result.Error<>(requestValidation.getErrorOrNull());
        }

        Long userId = updatedUser.getId();

        try {
            // Check if user exists
            Result<User> userValidation = validateUserExists(userId);
            if (userValidation.isError()) {
                return new Result.Error<>(userValidation.getErrorOrNull());
            }

            User existingUser = userValidation.getValueOrNull();

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

            if (updatedUser.getEmail() != null && !updatedUser.getEmail().trim().isEmpty()) {
                existingUser.setEmail(updatedUser.getEmail());
            }

            existingUser.setUpdatedAt(LocalDateTime.now());

            // Save
            User savedUser = userRepository.save(existingUser);
            savedUser.setPassword(null); // hide password

            return new Result.Success<>(savedUser);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    // Get All Users
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


    // Delete User
    public Result<Void> deleteUser(Long userId) {
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




    public Result<User> getUserByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return new Result.Error<>(new FieldRequiredError("Email"));
            }
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