package com.college.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidateRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Position is required")
    private Long positionId;

    private String manifesto;

    private String slogan;

    private String photoUrl;
}
