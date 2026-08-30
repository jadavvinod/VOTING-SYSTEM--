package com.college.voting.controller;

import com.college.voting.dto.VoteRequest;
import com.college.voting.dto.VoteResponse;
import com.college.voting.dto.VotingStatusResponse;
import com.college.voting.entity.Student;
import com.college.voting.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Enforced as ROLE_STUDENT via SecurityConfig ("/api/votes/**"). The Student principal
 * is resolved by Spring Security from the validated JWT — this is the ONLY source of
 * truth for "who is voting". Nothing here ever accepts a student/election ID meant to
 * identify the voter from the request body.
 */
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public VoteResponse castVote(@AuthenticationPrincipal Student student, @Valid @RequestBody VoteRequest request) {
        return voteService.castVote(student, request);
    }

    @GetMapping("/status/{electionId}")
    public List<VotingStatusResponse> votingStatus(@AuthenticationPrincipal Student student,
                                                     @PathVariable Long electionId) {
        return voteService.getVotingStatus(student, electionId);
    }
}
