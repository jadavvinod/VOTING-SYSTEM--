package com.college.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingStatusResponse {
    private Long positionId;
    private String positionTitle;
    private boolean hasVoted;
    // Deliberately no "votedForCandidateId" field anywhere in this DTO or any other
    // student-facing response — ballot privacy: a student can see THAT they voted,
    // never WHAT they voted for, once cast.
}
