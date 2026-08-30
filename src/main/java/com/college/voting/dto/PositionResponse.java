package com.college.voting.dto;

import com.college.voting.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private Long id;
    private String title;
    private Long electionId;

    public static PositionResponse fromEntity(Position p) {
        return PositionResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .electionId(p.getElection().getId())
                .build();
    }
}
