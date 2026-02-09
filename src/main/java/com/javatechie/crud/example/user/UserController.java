package com.javatechie.crud.example.user;
import com.javatechie.crud.example.response.ApiResponse;
import com.javatechie.crud.example.response.ErrorType;
import com.javatechie.crud.example.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<User>> getProfile(@PathVariable String id) {
        Result<User> result = userService.getProfile(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "Profile fetched successfully"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }


    @PostMapping("/update-profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody User updatedUser) {
        Result<User> result = userService.updateProfile(updatedUser);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "Profile updated successfully"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }


    @GetMapping("/get-all-users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        Result<List<User>> result = userService.getAllUsers();
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "Users fetched successfully"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }


    @DeleteMapping("delete-user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        Result<Void> result = userService.deleteUser(userId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        Result<User> result = userService.getUserByEmail(email);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "User retrieved successfully"));
        } else {
            return handleErrorResult(result.getErrorOrNull());
        }
    }

    // Helper Methods
    private <T> ResponseEntity<ApiResponse<T>> handleErrorResult(ErrorType error) {
        return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
    }
}