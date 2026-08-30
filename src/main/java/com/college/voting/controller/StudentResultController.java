package com.college.voting.controller;

import com.college.voting.dto.ElectionResultResponse;
import com.college.voting.entity.Election;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.exception.ElectionNotActiveException;
import com.college.voting.service.ElectionService;
import com.college.voting.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/elections")
@RequiredArgsConstructor
public class StudentResultController {

    private final ResultService resultService;
    private final ElectionService electionService;

    @GetMapping("/{electionId}/results")
    public ElectionResultResponse results(@PathVariable Long electionId) {
        Election election = electionService.getElectionEntity(electionId);

        if (election.getEffectiveStatus() != ElectionStatus.COMPLETED) {
            throw new ElectionNotActiveException(
                    "Results are not yet available. Results will be visible after the election ends.");
        }

        return resultService.calculateResults(electionId);
    }
}