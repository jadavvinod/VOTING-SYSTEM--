package com.college.voting.controller;

import com.college.voting.dto.ChangePasswordRequest;
import com.college.voting.dto.MessageResponse;
import com.college.voting.dto.StudentResponse;
import com.college.voting.dto.UpdateProfileRequest;
import com.college.voting.entity.Student;
import com.college.voting.exception.UnauthorizedException;
import com.college.voting.repository.StudentRepository;
import com.college.voting.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentRepository studentRepository;
    private final AuthService authService;

    // GET PROFILE
    @GetMapping
    public StudentResponse getProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Student is not authenticated");
        }

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Student account not found"));

        return StudentResponse.fromEntity(student);
    }

    // UPDATE PROFILE
    @PutMapping
    public StudentResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Student is not authenticated");
        }

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Student account not found"));

        return authService.updateProfile(student, request);
    }

    // CHANGE PASSWORD
    @PutMapping("/change-password")
    public MessageResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Student is not authenticated");
        }

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Student account not found"));

        return authService.changePassword(student, request);
    }
}