package com.college.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResultResponse {
    private Long positionId;
    private String positionTitle;
    private long totalVotes;
    private boolean isTie;
    private List<CandidateResultResponse> candidates;
}
