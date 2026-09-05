package com.college.voting.service;

import com.college.voting.dto.*;
import com.college.voting.entity.Admin;
import com.college.voting.entity.OtpPurpose;
import com.college.voting.entity.Student;
import com.college.voting.exception.DuplicateResourceException;
import com.college.voting.exception.UnauthorizedException;
import com.college.voting.repository.StudentRepository;
import com.college.voting.security.CustomUserDetailsService;
import com.college.voting.security.JwtUtil;
import com.college.voting.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final OtpService otpService;

    @Transactional
    public StudentResponse registerStudent(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "Password and confirm password do not match"
            );
        }

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException(
                    "Student ID is already registered"
            );
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .course(request.getCourse())
                .year(request.getYear())
                .phoneNumber(request.getPhoneNumber())
                .status(Student.AccountStatus.ACTIVE)
                .build();

        Student saved = studentRepository.save(student);

        return StudentResponse.fromEntity(saved);
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail();

        if (loginAttemptService.isLocked(email)) {
            throw new UnauthorizedException(
                    "Too many failed login attempts. Try again in "
                            + loginAttemptService.minutesUntilUnlock(email)
                            + " minute(s)."
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailure(email);
            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        loginAttemptService.recordSuccess(email);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String role;
        String fullName;

        if (userDetails instanceof Student student) {

            role = student.getRole().name();
            fullName = student.getFullName();

        } else if (userDetails instanceof Admin admin) {

            role = admin.getRole().name();
            fullName = admin.getFullName();

        } else {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        String token = jwtUtil.generateToken(userDetails, role);

        return LoginResponse.builder()
                .token(token)
                .role(role)
                .fullName(fullName)
                .email(email)
                .build();
    }

    /**
     * Starts the forgot-password flow.
     */
    @Transactional
    public MessageResponse forgotPassword(
            ForgotPasswordRequest request) {

        boolean exists =
                studentRepository.existsByEmail(request.getEmail());

        String genericMessage =
                "If an account exists for that email, a reset code has been sent.";

        if (!exists) {
            return MessageResponse.builder()
                    .message(genericMessage)
                    .build();
        }

        String otp =
                otpService.generateOtp(
                        request.getEmail(),
                        OtpPurpose.PASSWORD_RESET
                );

        return MessageResponse.builder()
                .message(genericMessage)
                .devOnlyOtp(otp)
                .build();
    }

    @Transactional
    public MessageResponse resetPassword(
            ResetPasswordRequest request) {

        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }

        otpService.verifyOtp(
                request.getEmail(),
                OtpPurpose.PASSWORD_RESET,
                request.getOtp()
        );

        Student student =
                studentRepository.findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid request"
                                )
                        );

        student.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        studentRepository.save(student);

        loginAttemptService.recordSuccess(
                request.getEmail()
        );

        return MessageResponse.builder()
                .message(
                        "Password reset successfully. You can now log in."
                )
                .build();
    }

    @Transactional
    public MessageResponse changePassword(
            Student student,
            ChangePasswordRequest request) {

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                student.getPassword()
        )) {

            throw new UnauthorizedException(
                    "Current password is incorrect"
            );
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }

        student.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        studentRepository.save(student);

        return MessageResponse.builder()
                .message("Password changed successfully.")
                .build();
    }

    @Transactional
    public StudentResponse updateProfile(
            Student student,
            UpdateProfileRequest request) {

        student.setFullName(request.getFullName());
        student.setDepartment(request.getDepartment());
        student.setCourse(request.getCourse());
        student.setYear(request.getYear());
        student.setPhoneNumber(request.getPhoneNumber());

        // Save profile picture if provided
        if (request.getProfileImage() != null
                && !request.getProfileImage().isBlank()) {

            student.setProfileImage(
                    request.getProfileImage()
            );
        }

        Student savedStudent =
                studentRepository.save(student);

        return StudentResponse.fromEntity(savedStudent);
    }
}