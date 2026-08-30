package com.college.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResultResponse {
    private Long candidateId;
    private String candidateName;
    private long votes;
    private double percentage;
    private boolean winner;
}
