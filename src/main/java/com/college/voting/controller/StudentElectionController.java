package com.college.voting.controller;

import com.college.voting.dto.ElectionResponse;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.entity.Student;
import com.college.voting.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only election browsing for logged-in students.
 *
 * Completed elections can be hidden from an individual student's list.
 * The actual election and voting data are never deleted by the student.
 */
@RestController
@RequestMapping("/api/student/elections")
@RequiredArgsConstructor
public class StudentElectionController {

    private final ElectionService electionService;

    @GetMapping("/active")
    public List<ElectionResponse> active() {
        return electionService.listByEffectiveStatus(
                ElectionStatus.ACTIVE
        );
    }

    @GetMapping("/upcoming")
    public List<ElectionResponse> upcoming() {
        return electionService.listByEffectiveStatus(
                ElectionStatus.UPCOMING
        );
    }

    @GetMapping("/completed")
    public List<ElectionResponse> completed(
            @AuthenticationPrincipal Student student) {

        return electionService.listByEffectiveStatusForStudent(
                ElectionStatus.COMPLETED,
                student
        );
    }

    /**
     * Remove/hide a completed election from this student's list.
     *
     * DELETE:
     * /api/student/elections/completed/{electionId}
     */
    @DeleteMapping("/completed/{electionId}")
    public void removeCompletedElection(
            @AuthenticationPrincipal Student student,
            @PathVariable Long electionId) {

        electionService.hideCompletedElection(
                electionId,
                student
        );
    }

    @GetMapping("/{id}")
    public ElectionResponse get(@PathVariable Long id) {
        return electionService.getElection(id);
    }
}