package com.college.voting.controller;

import com.college.voting.dto.ElectionResponse;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only election browsing for logged-in students. Enforced as ROLE_STUDENT
 * via SecurityConfig ("/api/student/**").
 */
@RestController
@RequestMapping("/api/student/elections")
@RequiredArgsConstructor
public class StudentElectionController {

    private final ElectionService electionService;

    @GetMapping("/active")
    public List<ElectionResponse> active() {
        return electionService.listByEffectiveStatus(ElectionStatus.ACTIVE);
    }

    @GetMapping("/upcoming")
    public List<ElectionResponse> upcoming() {
        return electionService.listByEffectiveStatus(ElectionStatus.UPCOMING);
    }

    @GetMapping("/completed")
    public List<ElectionResponse> completed() {
        return electionService.listByEffectiveStatus(ElectionStatus.COMPLETED);
    }

    @GetMapping("/{id}")
    public ElectionResponse get(@PathVariable Long id) {
        return electionService.getElection(id);
    }
}
