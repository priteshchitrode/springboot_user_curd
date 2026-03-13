package com.priteshchittrode.user_crud.phone_verification;

import com.priteshchittrode.user_crud.user.User;
import com.priteshchittrode.user_crud.user.UserRepository;
import com.priteshchittrode.user_crud.response.Result;
import com.priteshchittrode.user_crud.response.ErrorType.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhoneService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final UserRepository userRepository;

    // Inject your SMS provider here (e.g., Twilio, AWS SNS, Fast2SMS)
    // private final SmsProvider smsProvider;

    private static final SecureRandom random = new SecureRandom();


    /// Send OTP
    public Result<String> sendOtpOnPhone(Map<String, String> request) {
        try {
            String userIdStr = request.get("userId");
            String phone = request.get("phone");

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("userId is required"));
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new Result.Error<>(new BadRequestError("Invalid userId format"));
            }

            if (phone == null || phone.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("phone is required"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (!user.getPhone().equals(phone)) {
                return new Result.Error<>(new BadRequestError("Phone does not match user"));
            }

            if (user.isPhoneVerified()) {
                return new Result.Error<>(new BadRequestError("Phone is already verified"));
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", random.nextInt(999999));
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

            // Delete existing token if present
            phoneVerificationRepository.findByUserId(userId)
                    .ifPresent(phoneVerificationRepository::delete);

            // Create new verification entity
            PhoneVerificationEntity verificationEntity = new PhoneVerificationEntity();
            verificationEntity.setUser(user);
            verificationEntity.setOtp(otp);
            verificationEntity.setExpiryDate(expiryDate);
            verificationEntity.setIsUsed(false);

            phoneVerificationRepository.save(verificationEntity);

            // TODO: Send OTP via SMS provider
            // smsProvider.sendSms(phone, "Your verification OTP is: " + otp + ". Valid for 10 minutes.");

            return new Result.Success<>(otp);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    /// Verify OTP
    public Result<Void> verifyPhoneOtp(Map<String, String> request) {
        try {
            String userIdStr = request.get("userId");
            String phone = request.get("phone");
            String otp = request.get("otp");

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("userId is required"));
            }

            long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new Result.Error<>(new BadRequestError("Invalid userId format"));
            }

            if (phone == null || phone.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("phone is required"));
            }

            if (otp == null || otp.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("OTP is required"));
            }

            PhoneVerificationEntity verificationEntity =
                    phoneVerificationRepository.findByUserId(userId).orElse(null);

            if (verificationEntity == null) {
                return new Result.Error<>(new InvalidTokenError());
            }

            if (verificationEntity.getIsUsed()) {
                return new Result.Error<>(new BadRequestError("OTP has already been used"));
            }

            if (LocalDateTime.now().isAfter(verificationEntity.getExpiryDate())) {
                return new Result.Error<>(new TokenExpiredError());
            }

            User user = verificationEntity.getUser();

            if (!user.getPhone().equals(phone)) {
                return new Result.Error<>(new BadRequestError("Phone does not match"));
            }

            if (!verificationEntity.getOtp().equals(otp)) {
                verificationEntity.setAttemptCount(verificationEntity.getAttemptCount() + 1);
                phoneVerificationRepository.save(verificationEntity);
                return new Result.Error<>(new BadRequestError("Invalid OTP"));
            }

            // Update user
            user.setPhoneVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Mark token as used
            verificationEntity.setIsUsed(true);
            verificationEntity.setVerifiedAt(LocalDateTime.now());
            verificationEntity.setAttemptCount(verificationEntity.getAttemptCount() + 1);
            phoneVerificationRepository.save(verificationEntity);

            return new Result.Success<>(null);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    /// Resend OTP
    public Result<String> resendOtpOnPhone(Map<String, String> request) {
        try {
            String userIdStr = request.get("userId");
            String phone = request.get("phone");

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("userId is required"));
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new Result.Error<>(new BadRequestError("Invalid userId format"));
            }

            if (phone == null || phone.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("phone is required"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (!user.getPhone().equals(phone)) {
                return new Result.Error<>(new BadRequestError("Phone does not match user"));
            }

            if (user.isPhoneVerified()) {
                return new Result.Error<>(new BadRequestError("Phone is already verified"));
            }

            // Generate new 6-digit OTP
            String newOtp = String.format("%06d", random.nextInt(999999));
            LocalDateTime newExpiryDate = LocalDateTime.now().plusMinutes(10);

            PhoneVerificationEntity verificationEntity =
                    phoneVerificationRepository.findByUserId(userId).orElse(null);

            if (verificationEntity == null) {
                verificationEntity = new PhoneVerificationEntity();
                verificationEntity.setUser(user);
            }

            verificationEntity.setOtp(newOtp);
            verificationEntity.setExpiryDate(newExpiryDate);
            verificationEntity.setIsUsed(false);
            verificationEntity.setAttemptCount(0);
            verificationEntity.setCreatedAt(LocalDateTime.now());
            verificationEntity.setVerifiedAt(null);

            phoneVerificationRepository.save(verificationEntity);

            // TODO: Send OTP via SMS provider
            // smsProvider.sendSms(phone, "Your verification OTP is: " + newOtp + ". Valid for 10 minutes.");

            return new Result.Success<>(newOtp);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }
}