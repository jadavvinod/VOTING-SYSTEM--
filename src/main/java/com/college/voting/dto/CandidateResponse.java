package com.college.voting.dto;

import com.college.voting.entity.Candidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private String studentId;
    private String fullName;
    private String department;
    private String course;
    private String year;
    private Long positionId;
    private String positionTitle;
    private Long electionId;
    private String photoUrl;
    private String manifesto;
    private String slogan;
    private String status;

    public static CandidateResponse fromEntity(Candidate c) {
        return CandidateResponse.builder()
                .id(c.getId())
                .studentId(c.getStudent().getStudentId())
                .fullName(c.getStudent().getFullName())
                .department(c.getStudent().getDepartment())
                .course(c.getStudent().getCourse())
                .year(c.getStudent().getYear())
                .positionId(c.getPosition().getId())
                .positionTitle(c.getPosition().getTitle())
                .electionId(c.getElection().getId())
                .photoUrl(c.getPhotoUrl())
                .manifesto(c.getManifesto())
                .slogan(c.getSlogan())
                .status(c.getStatus().name())
                .build();
    }
}
