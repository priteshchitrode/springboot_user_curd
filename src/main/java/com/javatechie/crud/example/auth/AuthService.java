package com.javatechie.crud.example.auth;

import com.javatechie.crud.example.user.User;
import com.javatechie.crud.example.user.UserRepository;
import com.javatechie.crud.example.response.Result;
import com.javatechie.crud.example.response.ErrorType;
import com.javatechie.crud.example.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Authentication Service
 * Uses ErrorType static nested classes (all in one file)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ============ Validation Methods ============

    private Result<Void> validateSignUpRequest(String firstName, String lastName, String email, String password) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("First name"));
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("Last name"));
        }

        if (email == null || email.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("Email"));
        }

        if (!isValidEmail(email)) {
            return new Result.Error<>(new ErrorType.InvalidEmailError());
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return new Result.Error<>(new ErrorType.DuplicateEmailError());
        }

        if (password == null || password.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("Password"));
        }

        if (password.length() < 6) {
            return new Result.Error<>(new ErrorType.InvalidPasswordError("Password must be at least 6 characters"));
        }

        return new Result.Success<>(null);
    }

    private Result<Void> validateSignInRequest(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("Email"));
        }

        if (password == null || password.trim().isEmpty()) {
            return new Result.Error<>(new ErrorType.FieldRequiredError("Password"));
        }

        return new Result.Success<>(null);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-z0-9+_.-]+@[a-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // ============ Authentication Methods ============

    public Result<AuthResponse> signUp(String firstName, String lastName, String email, String password) {
        Result<Void> validationResult = validateSignUpRequest(firstName, lastName, email, password);
        if (validationResult.isError()) {
            return new Result.Error<>(validationResult.getErrorOrNull());
        }

        try {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            String accessToken = jwtUtil.generateAccessToken(savedUser.getId());
            String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId());

            savedUser.setRefreshToken(refreshToken);
            userRepository.save(savedUser);

            savedUser.setPassword(null);
            AuthResponse authResponse = new AuthResponse(savedUser, accessToken, refreshToken);

            return new Result.Success<>(authResponse);

        } catch (Exception e) {
            return new Result.Error<>(new ErrorType.InternalServerError(e.getMessage()));
        }
    }

    public Result<AuthResponse> signIn(String email, String password) {
        Result<Void> validationResult = validateSignInRequest(email, password);
        if (validationResult.isError()) {
            return new Result.Error<>(validationResult.getErrorOrNull());
        }

        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                return new Result.Error<>(new ErrorType.InvalidCredentialsError());
            }

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            user.setPassword(null);
            AuthResponse authResponse = new AuthResponse(user, accessToken, refreshToken);

            return new Result.Success<>(authResponse);

        } catch (Exception e) {
            return new Result.Error<>(new ErrorType.InternalServerError(e.getMessage()));
        }
    }

    public Result<String> refreshToken(Long userId, String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return new Result.Error<>(new ErrorType.MissingHeaderError("Refresh Token"));
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                return new Result.Error<>(new ErrorType.TokenExpiredError());
            }

            Long tokenUserId;
            try {
                tokenUserId = jwtUtil.extractUserId(refreshToken);
            } catch (Exception e) {
                return new Result.Error<>(new ErrorType.InvalidTokenError());
            }

            if (!tokenUserId.equals(userId)) {
                return new Result.Error<>(new ErrorType.ForbiddenError("Token does not belong to provided user"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRefreshToken() == null || !refreshToken.equals(user.getRefreshToken())) {
                return new Result.Error<>(new ErrorType.RefreshTokenMismatchError());
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId);
            return new Result.Success<>(newAccessToken);

        } catch (Exception e) {
            return new Result.Error<>(new ErrorType.InternalServerError(e.getMessage()));
        }
    }

    public Result<Void> logout(Long userId, Long currentUserId) {
        try {
            if (!userId.equals(currentUserId)) {
                return new Result.Error<>(new ErrorType.AccessDeniedError());
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setRefreshToken(null);
                userRepository.save(user);
            }

            return new Result.Success<>(null);

        } catch (Exception e) {
            return new Result.Error<>(new ErrorType.InternalServerError(e.getMessage()));
        }
    }
}