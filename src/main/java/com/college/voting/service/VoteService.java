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
     * Casts one vote.
     *
     * Rules:
     *
     * 1. Election must be ACTIVE.
     * 2. Student must belong to the election department.
     * 3. ALL / All Departments elections are available to everyone.
     * 4. Candidate must belong to the selected position.
     * 5. Candidate must be APPROVED.
     * 6. Student cannot vote twice for the same position.
     * 7. Database unique constraint protects against concurrent duplicate votes.
     */
    @Transactional
    public VoteResponse castVote(
            Student student,
            VoteRequest request) {

        /*
         * =========================================================
         * 1. GET POSITION
         * =========================================================
         */

        Position position = positionRepository
                .findById(request.getPositionId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Position not found"));

        /*
         * Get election from position.
         */

        Election election = position.getElection();

        /*
         * =========================================================
         * 2. CHECK ELECTION STATUS
         * =========================================================
         */

        if (election.getEffectiveStatus()
                != ElectionStatus.ACTIVE) {

            throw new ElectionNotActiveException(
                    "Voting is not currently open for this election.");
        }

        /*
         * =========================================================
         * 3. DEPARTMENT ELIGIBILITY CHECK
         * =========================================================
         *
         * Examples:
         *
         * Election = ALL
         * Student  = CSE
         * Result   = ALLOW
         *
         * Election = All Departments
         * Student  = CSE
         * Result   = ALLOW
         *
         * Election = blank/null
         * Student  = CSE
         * Result   = ALLOW
         *
         * Election = CSE
         * Student  = CSE
         * Result   = ALLOW
         *
         * Election = ECE
         * Student  = CSE
         * Result   = REJECT
         */

        String electionDepartment =
                election.getDepartment();

        String studentDepartment =
                student.getDepartment();

        /*
         * Normalize election department.
         */

        String normalizedElectionDepartment =
                electionDepartment == null
                        ? ""
                        : electionDepartment.trim();

        /*
         * Normalize student department.
         */

        String normalizedStudentDepartment =
                studentDepartment == null
                        ? ""
                        : studentDepartment.trim();

        /*
         * Check whether this is an All Departments election.
         */

        boolean isAllDepartments =
                normalizedElectionDepartment.isEmpty()
                || normalizedElectionDepartment
                        .equalsIgnoreCase("ALL")
                || normalizedElectionDepartment
                        .equalsIgnoreCase("ALL DEPARTMENTS");

        /*
         * If election is NOT for all departments,
         * then student department must match.
         */

        if (!isAllDepartments
                && !normalizedElectionDepartment
                        .equalsIgnoreCase(
                                normalizedStudentDepartment)) {

            throw new IllegalArgumentException(
                    "You are not eligible to vote in this election.");
        }

        /*
         * =========================================================
         * 4. GET CANDIDATE
         * =========================================================
         */

        Candidate candidate =
                candidateService.getCandidateEntity(
                        request.getCandidateId());

        /*
         * =========================================================
         * 5. CHECK CANDIDATE POSITION
         * =========================================================
         *
         * Candidate must belong to the position
         * selected by the student.
         */

        if (!candidate.getPosition()
                .getId()
                .equals(position.getId())) {

            throw new IllegalArgumentException(
                    "This candidate is not running for the selected position.");
        }

        /*
         * =========================================================
         * 6. CHECK CANDIDATE APPROVAL
         * =========================================================
         */

        if (candidate.getStatus()
                != CandidateStatus.APPROVED) {

            throw new IllegalArgumentException(
                    "This candidate is not approved to receive votes.");
        }

        /*
         * =========================================================
         * 7. CHECK DUPLICATE VOTE
         * =========================================================
         *
         * One student can vote only once for one position.
         */

        if (voteRepository.existsByStudentIdAndPositionId(
                student.getId(),
                position.getId())) {

            throw new AlreadyVotedException(
                    "You have already voted for this position.");
        }

        /*
         * =========================================================
         * 8. CREATE VOTE
         * =========================================================
         */

        Vote vote = Vote.builder()
                .student(student)
                .election(election)
                .position(position)
                .candidate(candidate)
                .build();

        /*
         * =========================================================
         * 9. SAVE VOTE
         * =========================================================
         */

        try {

            voteRepository.save(vote);

            /*
             * Flush immediately.
             *
             * This helps detect duplicate votes caused by
             * concurrent requests/database constraints.
             */

            entityManager.flush();

        } catch (DataIntegrityViolationException ex) {

            throw new AlreadyVotedException(
                    "You have already voted for this position.");
        }

        /*
         * =========================================================
         * 10. SUCCESS RESPONSE
         * =========================================================
         */

        return VoteResponse.builder()
                .success(true)
                .positionTitle(position.getTitle())
                .message(
                        "Your vote has been successfully recorded.")
                .build();
    }

    /**
     * Returns voting status for a student
     * across all positions in an election.
     *
     * The selected candidate is NOT exposed.
     */
    public List<VotingStatusResponse> getVotingStatus(
            Student student,
            Long electionId) {

        /*
         * Get all positions for election.
         */

        List<Position> positions =
                positionRepository.findByElectionId(
                        electionId);

        /*
         * Get votes already cast by this student.
         */

        List<Vote> studentVotes =
                voteRepository.findByStudentIdAndElectionId(
                        student.getId(),
                        electionId);

        /*
         * Extract position IDs for which
         * student has already voted.
         */

        List<Long> votedPositionIds =
                studentVotes.stream()
                        .map(v -> v.getPosition().getId())
                        .collect(Collectors.toList());

        /*
         * Build voting status response.
         */

        return positions.stream()
                .map(p ->
                        VotingStatusResponse.builder()
                                .positionId(p.getId())
                                .positionTitle(p.getTitle())
                                .hasVoted(
                                        votedPositionIds
                                                .contains(p.getId()))
                                .build()
                )
                .collect(Collectors.toList());
    }
}