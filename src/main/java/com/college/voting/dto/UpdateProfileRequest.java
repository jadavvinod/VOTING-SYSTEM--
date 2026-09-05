package com.college.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 80, message = "Department must not exceed 80 characters")
    private String department;

    @Size(max = 80, message = "Course must not exceed 80 characters")
    private String course;

    @Size(max = 20, message = "Year must not exceed 20 characters")
    private String year;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    private String profileImage;
}