package com.college.voting.service;

import com.college.voting.dto.VoteRequest;
import com.college.voting.dto.VoteResponse;
import com.college.voting.dto.VotingStatusResponse;
import com.college.voting.entity.*;
import com.college.voting.exception.AlreadyVotedException;
import com.college.voting.exception.ElectionNotActiveException;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.PositionRepository;
import com.college.voting.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final PositionRepository positionRepository;
    private final CandidateService candidateService;
    private final EntityManager entityManager;

    /**
     * Casts one vote. The student comes from the authenticated principal (passed in by
     * the controller from the SecurityContext) — never from the request body. Every
     * rule from the spec is enforced here, not just on the frontend:
     *  1. Election must be ACTIVE right now (effective status, time-based, not the stored flag)
     *  2. Candidate must belong to the given position
     *  3. Candidate must be APPROVED
     *  4. Student must not have already voted for this position (checked here AND
     *     backstopped by the DB unique constraint for concurrent requests)
     */
    @Transactional
    public VoteResponse castVote(Student student, VoteRequest request) {
        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        Election election = position.getElection();

        if (election.getEffectiveStatus() != ElectionStatus.ACTIVE) {
            throw new ElectionNotActiveException(
                    "Voting is not currently open for this election.");
        }

        Candidate candidate = candidateService.getCandidateEntity(request.getCandidateId());

        if (!candidate.getPosition().getId().equals(position.getId())) {
            throw new IllegalArgumentException("This candidate is not running for the selected position.");
        }

        if (candidate.getStatus() != CandidateStatus.APPROVED) {
            throw new IllegalArgumentException("This candidate is not approved to receive votes.");
        }

        // Pre-check for a fast, friendly error message in the common (non-race) case.
        if (voteRepository.existsByStudentIdAndPositionId(student.getId(), position.getId())) {
            throw new AlreadyVotedException("You have already voted for this position.");
        }

        Vote vote = Vote.builder()
                .student(student)
                .election(election)
                .position(position)
                .candidate(candidate)
                .build();

        try {
            voteRepository.save(vote);
            // Flush now so a concurrent duplicate insert is caught inside THIS
            // transaction, as a DataIntegrityViolationException, rather than
            // silently succeeding and being caught only at commit time.
            entityManager.flush();
        } catch (DataIntegrityViolationException ex) {
            // The DB unique constraint caught a duplicate vote submitted concurrently
            // (e.g. accidental double-click / double request) that slipped past the
            // pre-check above. This is the backstop the spec requires.
            throw new AlreadyVotedException("You have already voted for this position.");
        }

        return VoteResponse.builder()
                .success(true)
                .positionTitle(position.getTitle())
                .message("Your vote has been successfully recorded.")
                .build();
    }

    /**
     * Voting status for a student across all positions in an election — used to render
     * "voted / not voted" per position on the ballot. NEVER exposes which candidate was
     * chosen; ballot privacy is preserved by keeping candidate choice out of this DTO
     * entirely (see VotingStatusResponse).
     */
    public List<VotingStatusResponse> getVotingStatus(Student student, Long electionId) {
        List<Position> positions = positionRepository.findByElectionId(electionId);
        List<Vote> studentVotes = voteRepository.findByStudentIdAndElectionId(student.getId(), electionId);
        List<Long> votedPositionIds = studentVotes.stream()
                .map(v -> v.getPosition().getId())
                .collect(Collectors.toList());

        return positions.stream()
                .map(p -> VotingStatusResponse.builder()
                        .positionId(p.getId())
                        .positionTitle(p.getTitle())
                        .hasVoted(votedPositionIds.contains(p.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
