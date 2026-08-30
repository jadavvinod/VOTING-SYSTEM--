package com.college.voting.controller;

import com.college.voting.dto.CandidateResponse;
import com.college.voting.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/candidates")
@RequiredArgsConstructor
public class StudentCandidateController {

    private final CandidateService candidateService;

    @GetMapping("/election/{electionId}")
    public List<CandidateResponse> byElection(@PathVariable Long electionId) {
        return candidateService.listApprovedByElection(electionId);
    }

    @GetMapping("/position/{positionId}")
    public List<CandidateResponse> byPosition(@PathVariable Long positionId) {
        return candidateService.listApprovedByPosition(positionId);
    }

    @GetMapping("/{id}")
    public CandidateResponse get(@PathVariable Long id) {
        return candidateService.getCandidate(id);
    }
}
