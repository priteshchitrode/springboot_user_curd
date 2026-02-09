package com.priteshchittrode.user_crud.auth;
import com.priteshchittrode.user_crud.response.ApiResponse;
import com.priteshchittrode.user_crud.response.Result;
import com.priteshchittrode.user_crud.response.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /// Apis
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(@RequestBody Map<String, String> request) {
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String email = request.get("email");
        String password = request.get("password");
        Result<AuthResponse> result = authService.signUp(firstName, lastName, email, password);
        return handleAuthResult(result, "User registered successfully");
    }


    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<AuthResponse>> signIn(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        Result<AuthResponse> result = authService.signIn(email, password);
        return handleAuthResult(result, "Login successful");
    }


    @PostMapping("/refresh-token/{userId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@PathVariable Long userId, HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        // Extract token from Bearer header
        String refreshToken = extractBearerToken(header);
        if (refreshToken == null) {
            ErrorType error = new ErrorType.MissingHeaderError("Authorization Bearer Token");
            return handleErrorResult(error);
        }

        Result<String> result = authService.refreshToken(userId, refreshToken);
        if (result.isSuccess()) {
            Map<String, String> data = new HashMap<>();
            data.put("accessToken", result.getValueOrNull());
            return ResponseEntity.ok(ApiResponse.success(data, "Access token generated"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }


    @PostMapping("/logout/{userId}")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable Long userId) {
        Result<Void> result = authService.logout(userId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    ///  Helper Methods
    private ResponseEntity<ApiResponse<AuthResponse>> handleAuthResult(Result<AuthResponse> result, String successMessage) {
        if (result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(result.getValueOrNull(), successMessage));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus())
                    .body(ApiResponse.error(error.getMessage()));
        }
    }


    private <T> ResponseEntity<ApiResponse<T>> handleErrorResult(ErrorType error) {
        return ResponseEntity.status(error.getHttpStatus())
                .body(ApiResponse.error(error.getMessage()));
    }


    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7).trim();
    }

}