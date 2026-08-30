package com.college.voting.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ElectionRequest {

    @NotBlank(message = "Election name is required")
    private String name;

    private String description;

    private String academicYear;

    @NotNull(message = "Start date/time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date/time is required")
    private LocalDateTime endDateTime;

    private String rules;

    private boolean showLiveResults;
}
