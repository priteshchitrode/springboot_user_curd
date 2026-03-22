package com.priteshchittrode.user_crud.email;
import com.priteshchittrode.user_crud.response.ApiResponse;
import com.priteshchittrode.user_crud.response.ErrorType;
import com.priteshchittrode.user_crud.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody Map<String, String> request) {
        Result<String> result = emailService.sendOtpOnEmail(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "Verification OTP resent successfully on your email"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody Map<String, String> request) {
        Result<Void> result = emailService.verifyEmailOtp(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestBody Map<String, String> request) {
        Result<String> result = emailService.resendOtpOnEmail(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "Verification OTP resent successfully on your email"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }

}