package com.college.voting.repository;

import com.college.voting.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByStudentIdAndPositionId(Long studentId, Long positionId);

    List<Vote> findByStudentIdAndElectionId(Long studentId, Long electionId);

    long countByPositionIdAndCandidateId(Long positionId, Long candidateId);

    long countByElectionId(Long electionId);

    long countByElectionIdAndStudentIdIn(Long electionId, List<Long> studentIds);

    @Query("SELECT COUNT(DISTINCT v.student.id) FROM Vote v WHERE v.election.id = :electionId")
    long countDistinctVotersByElectionId(@Param("electionId") Long electionId);

    @Query("SELECT v.student.department, COUNT(DISTINCT v.student.id) FROM Vote v " +
           "WHERE v.election.id = :electionId GROUP BY v.student.department")
    List<Object[]> countDistinctVotersByDepartment(@Param("electionId") Long electionId);

    @Query("SELECT v.student.year, COUNT(DISTINCT v.student.id) FROM Vote v " +
           "WHERE v.election.id = :electionId GROUP BY v.student.year")
    List<Object[]> countDistinctVotersByYear(@Param("electionId") Long electionId);
}
