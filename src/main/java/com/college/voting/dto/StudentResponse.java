package com.college.voting.dto;

import com.college.voting.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String studentId;
    private String fullName;
    private String email;
    private String department;
    private String course;
    private String year;
    private String phoneNumber;
    private String profileImage;
    private String status;

    public static StudentResponse fromEntity(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .department(s.getDepartment())
                .course(s.getCourse())
                .year(s.getYear())
                .phoneNumber(s.getPhoneNumber())
                .profileImage(s.getProfileImage())
                .status(s.getStatus().name())
                .build();
    }
}