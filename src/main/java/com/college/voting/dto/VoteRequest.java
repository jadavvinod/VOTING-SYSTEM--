package com.college.voting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {

    @NotNull(message = "Position is required")
    private Long positionId;

    @NotNull(message = "Candidate is required")
    private Long candidateId;

    // Note: studentId is intentionally NOT here. The voting student is always
    // resolved from the authenticated SecurityContext principal in VoteController,
    // never trusted from the request body.
}
