package com.college.voting.service;

import com.college.voting.entity.OtpPurpose;
import com.college.voting.entity.OtpVerification;
import com.college.voting.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final long EXPIRY_MINUTES = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Generates and stores a hashed OTP. Returns the raw OTP so the caller can email it. */
    @Transactional
    public String generateOtp(String email, OtpPurpose purpose) {
        otpVerificationRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .ifPresent(last -> {
                    long secondsSinceLast = ChronoUnit.SECONDS.between(last.getCreatedAt(), LocalDateTime.now());
                    if (secondsSinceLast < RESEND_COOLDOWN_SECONDS) {
                        throw new IllegalStateException(
                                "Please wait " + (RESEND_COOLDOWN_SECONDS - secondsSinceLast) + " seconds before requesting another OTP.");
                    }
                });

        String rawOtp = generateSixDigitCode();

        OtpVerification otp = OtpVerification.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMinutes(EXPIRY_MINUTES)))
                .used(false)
                .build();

        otpVerificationRepository.save(otp);
        return rawOtp;
    }

    /** Verifies the OTP and marks it used. Throws if invalid, expired, or already used. */
    @Transactional
    public void verifyOtp(String email, OtpPurpose purpose, String rawOtp) {
        OtpVerification otp = otpVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new IllegalArgumentException("No OTP request found for this email."));

        if (otp.isUsed()) {
            throw new IllegalArgumentException("This OTP has already been used. Please request a new one.");
        }
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new IllegalArgumentException("This OTP has expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(rawOtp, otp.getOtpHash())) {
            throw new IllegalArgumentException("Incorrect OTP.");
        }

        otp.setUsed(true);
        otpVerificationRepository.save(otp);
    }

    private String generateSixDigitCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
