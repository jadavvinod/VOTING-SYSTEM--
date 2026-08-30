package com.college.voting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PositionRequest {

    @NotBlank(message = "Position title is required")
    private String title;
}
