package com.priteshchittrode.user_crud.phone_verification;

import com.priteshchittrode.user_crud.response.ApiResponse;
import com.priteshchittrode.user_crud.response.ErrorType;
import com.priteshchittrode.user_crud.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/phone")
@RequiredArgsConstructor
public class PhoneController {

    private final PhoneService phoneService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody Map<String, String> request) {
        Result<String> result = phoneService.sendOtpOnPhone(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "OTP sent successfully on your phone"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@RequestBody Map<String, String> request) {
        Result<Void> result = phoneService.verifyPhoneOtp(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null, "Phone verified successfully"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestBody Map<String, String> request) {
        Result<String> result = phoneService.resendOtpOnPhone(request);
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result.getValueOrNull(), "OTP resent successfully on your phone"));
        } else {
            ErrorType error = result.getErrorOrNull();
            return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(error.getMessage()));
        }
    }
}