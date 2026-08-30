package com.college.voting.repository;

import com.college.voting.entity.Candidate;
import com.college.voting.entity.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByElectionId(Long electionId);
    List<Candidate> findByPositionId(Long positionId);
    List<Candidate> findByPositionIdAndStatus(Long positionId, CandidateStatus status);
    List<Candidate> findByElectionIdAndStatus(Long electionId, CandidateStatus status);
    Optional<Candidate> findByStudentIdAndPositionId(Long studentId, Long positionId);
    boolean existsByStudentIdAndPositionId(Long studentId, Long positionId);
}
