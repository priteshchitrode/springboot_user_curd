package com.priteshchittrode.user_crud.auth;

import com.priteshchittrode.user_crud.response.Result;
import com.priteshchittrode.user_crud.response.ErrorType.*;
import com.priteshchittrode.user_crud.security.JwtUtil;
import com.priteshchittrode.user_crud.user.User;
import com.priteshchittrode.user_crud.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /// Validation for sign up
    private Result<Void> validateSignUpRequest(String firstName, String lastName, String email, String password) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("First name"));
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Last name"));
        }
        if (email == null || email.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Email"));
        }
        if (!isValidEmail(email)) {
            return new Result.Error<>(new InvalidEmailError());
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return new Result.Error<>(new DuplicateEmailError());
        }
        if (password == null || password.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Password"));
        }
        if (password.length() < 6) {
            return new Result.Error<>(new InvalidPasswordError("Password must be at least 6 characters"));
        }
        return new Result.Success<>(null);
    }


    /// Validation for sign in
    private Result<Void> validateSignInRequest(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Email"));
        }
        if (password == null || password.trim().isEmpty()) {
            return new Result.Error<>(new FieldRequiredError("Password"));
        }
        return new Result.Success<>(null);
    }


    /// Check Email is correct or not
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /// Signup Service
    public Result<AuthResponse> signUp(String firstName, String lastName, String email, String password) {
        try {
            Result<Void> validationResult = validateSignUpRequest(firstName, lastName, email, password);
            if (validationResult.isError()) {
                return new Result.Error<>(validationResult.getErrorOrNull());
            }

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
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    /// Sign-In Service
    public Result<AuthResponse> signIn(String email, String password) {
        try {
            Result<Void> validationResult = validateSignInRequest(email, password);
            if (validationResult.isError()) {
                return new Result.Error<>(validationResult.getErrorOrNull());
            }

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                return new Result.Error<>(new InvalidCredentialsError());
            }

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            user.setRefreshToken(refreshToken);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            user.setPassword(null);
            AuthResponse authResponse = new AuthResponse(user, accessToken, refreshToken);

            return new Result.Success<>(authResponse);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    /// Refresh Token Service
    public Result<String> refreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return new Result.Error<>(new MissingHeaderError("Refresh Token"));
            }

            if (!jwtUtil.isValidRefreshToken(refreshToken)) {
                return new Result.Error<>(new InvalidTokenError());
            }

            Long userId = jwtUtil.extractUserId(refreshToken);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (user.getRefreshToken() == null || !refreshToken.equals(user.getRefreshToken())) {
                return new Result.Error<>(new RefreshTokenMismatchError());
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId);
            return new Result.Success<>(newAccessToken);

        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    /// Logout Service
    public Result<Void> logout(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (user.getRefreshToken() == null) {
                return new Result.Error<>(new BadRequestError("User already logged out"));
            }

            user.setRefreshToken(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return new Result.Success<>(null);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


}