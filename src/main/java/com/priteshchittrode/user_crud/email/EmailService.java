package com.priteshchittrode.user_crud.email;

import com.priteshchittrode.user_crud.user.User;
import com.priteshchittrode.user_crud.user.UserRepository;
import com.priteshchittrode.user_crud.response.Result;
import com.priteshchittrode.user_crud.response.ErrorType.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final SecureRandom random = new SecureRandom();

    ///  Send Otp
    public Result<String> sendOtpOnEmail(Map<String, String> request) {
        try {
            String userIdStr = request.get("userId");
            String email = request.get("email");

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("userId is required"));
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new Result.Error<>(new BadRequestError("Invalid userId format"));
            }

            if (email == null || email.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("email is required"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (!user.getEmail().equals(email)) {
                return new Result.Error<>(new BadRequestError("Email does not match user"));
            }

            if (user.isEmailVerified()) {
                return new Result.Error<>(new BadRequestError("Email is already verified"));
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", random.nextInt(999999));

            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10); // Shorter expiry for OTP

            // Delete existing token if present
            emailVerificationRepository.findByUserId(userId).ifPresent(emailVerificationRepository::delete);

            // Create new verification token with OTP
            EmailVerificationEntity verificationToken = new EmailVerificationEntity();
            verificationToken.setUser(user);
            verificationToken.setOtp(otp);
            verificationToken.setExpiryDate(expiryDate);
            verificationToken.setIsUsed(false);

            emailVerificationRepository.save(verificationToken);

            // Send email with OTP
            sendEmail(user.getEmail(), "Email Verification OTP", buildVerificationEmailBody(user.getFirstName(), otp));

            return new Result.Success<>(otp);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    ///  Verify Email Otp
    public Result<Void> verifyEmailOtp(Map<String, String> request) {
        try {

            System.out.println("Verify Otp req: " + request);

            String userIdStr = request.get("userId");
            String email = request.get("email");
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

            if (email == null || email.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("email is required"));
            }

            if (otp == null || otp.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("OTP is required"));
            }

            EmailVerificationEntity verificationEntity = emailVerificationRepository.findByUserId(userId).orElse(null);

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

            if (!user.getEmail().equals(email)) {
                return new Result.Error<>(new BadRequestError("Email does not match"));
            }

            //  Actually validate the OTP value
            if (!verificationEntity.getOtp().equals(otp)) {
                verificationEntity.setAttemptCount(verificationEntity.getAttemptCount() + 1);
                emailVerificationRepository.save(verificationEntity);
                return new Result.Error<>(new BadRequestError("Invalid OTP"));
            }

            // Update user
            user.setEmailVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            //  Mark token as used and set verifiedAt
            verificationEntity.setIsUsed(true);
            verificationEntity.setVerifiedAt(LocalDateTime.now());
            verificationEntity.setAttemptCount(verificationEntity.getAttemptCount() + 1);
            emailVerificationRepository.save(verificationEntity);

            return new Result.Success<>(null);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    ///  Resend Otp
    public Result<String> resendOtpOnEmail(Map<String, String> request) {
        try {
            String userIdStr = request.get("userId");
            String email = request.get("email");

            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("userId is required"));
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new Result.Error<>(new BadRequestError("Invalid userId format"));
            }

            if (email == null || email.trim().isEmpty()) {
                return new Result.Error<>(new BadRequestError("email is required"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new Result.Error<>(new NotFoundError("User not found"));
            }

            if (!user.getEmail().equals(email)) {
                return new Result.Error<>(new BadRequestError("Email does not match user"));
            }

            if (user.isEmailVerified()) {
                return new Result.Error<>(new BadRequestError("Email is already verified"));
            }

            // Generate new 6-digit OTP
            String newOtp = String.format("%06d", random.nextInt(999999));
            LocalDateTime newExpiryDate = LocalDateTime.now().plusMinutes(10);

            EmailVerificationEntity emailVerificationEntity = emailVerificationRepository.findByUserId(userId).orElse(null);

            if (emailVerificationEntity == null) {
                // No existing token, create fresh one
                emailVerificationEntity = new EmailVerificationEntity();
                emailVerificationEntity.setUser(user);
            }

            // Update token with new OTP
            emailVerificationEntity.setOtp(newOtp);
            emailVerificationEntity.setExpiryDate(newExpiryDate);
            emailVerificationEntity.setIsUsed(false);
            emailVerificationEntity.setAttemptCount(0);
            emailVerificationEntity.setCreatedAt(LocalDateTime.now());
            emailVerificationEntity.setVerifiedAt(null);

            emailVerificationRepository.save(emailVerificationEntity);

            // Send email with new OTP
            sendEmail(user.getEmail(), "Resend: Email Verification OTP", buildVerificationEmailBody(user.getFirstName(), newOtp));

            return new Result.Success<>(newOtp);
        } catch (Exception e) {
            return new Result.Error<>(new InternalServerError(e.getMessage()));
        }
    }


    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // 👈 true = isHtml
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildVerificationEmailBody(String firstName, String otp) {
        return "<!DOCTYPE html>" +
                "<html lang='en'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Email Verification</title></head>" +
                "<body style='margin:0;padding:0;background-color:#f4f6f9;font-family:Georgia,serif;'>" +

                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f6f9;padding:30px 0;'>" +
                "<tr><td align='center'>" +

                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>" +

                // Header
                "<tr><td style='background:linear-gradient(135deg,#1a1a2e 0%,#16213e 100%);padding:30px;text-align:center;'>" +
                "<h1 style='margin:0;color:#ffffff;font-size:26px;font-weight:400;letter-spacing:2px;text-transform:uppercase;'>User CRUD</h1>" +
                "<p style='margin:6px 0 0;color:#a0aec0;font-size:13px;letter-spacing:1px;'>Account Verification</p>" +
                "</td></tr>" +

                // Body
                "<tr><td style='padding:38px 38px 22px;'>" +
                "<p style='margin:0 0 8px;color:#718096;font-size:13px;text-transform:uppercase;letter-spacing:1px;'>Hello,</p>" +
                "<h2 style='margin:0 0 24px;color:#1a1a2e;font-size:28px;font-weight:400;'>" + firstName + "</h2>" +
                "<p style='margin:0 0 32px;color:#4a5568;font-size:15px;line-height:1.8;'>" +
                "We received a request to verify your email address. Use the OTP below to complete your verification. " +
                "This code is valid for <strong>10 minutes</strong>." +
                "</p>" +

                // OTP Box
                "<table width='100%' cellpadding='0' cellspacing='0' style='margin-bottom:30px;'>" +
                "<tr><td align='center' style='background-color:#f7fafc;border:2px dashed #e2e8f0;border-radius:10px;padding:32px;'>" +
                "<p style='margin:0 0 8px;color:#718096;font-size:11px;text-transform:uppercase;letter-spacing:2px;'>Your OTP Code</p>" +
                "<p style='margin:0;color:#1a1a2e;font-size:42px;font-weight:700;letter-spacing:12px;font-family:monospace;'>" + otp + "</p>" +
                "</td></tr></table>" +

                "<p style='margin:0 0 12px;color:#4a5568;font-size:14px;line-height:1.7;'>" +
                "If you did not request this verification, you can safely ignore this email. " +
                "Your account will remain secure." +
                "</p>" +
                "</td></tr>" +

                // Footer
                "<tr><td style='background-color:#f7fafc;padding:24px 48px;border-top:1px solid #e2e8f0;text-align:center;'>" +
                "<p style='margin:0;color:#a0aec0;font-size:12px;line-height:1.6;'>" +
                "This is an automated message, please do not reply.<br>" +
                "&copy; 2025 User CRUD Application. All rights reserved." +
                "</p>" +
                "</td></tr>" +

                "</table>" +
                "</td></tr></table>" +
                "</body></html>";
    }
}