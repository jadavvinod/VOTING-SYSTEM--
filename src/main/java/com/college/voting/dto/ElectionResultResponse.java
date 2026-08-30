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
public class ElectionResultResponse {
    private Long electionId;
    private String electionName;
    private String status;
    private long totalEligibleStudents;
    private long totalVoters;
    private double turnoutPercentage;
    private List<PositionResultResponse> positions;
}
