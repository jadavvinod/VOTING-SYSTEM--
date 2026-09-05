package com.college.voting.repository;

import com.college.voting.entity.StudentHiddenElection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentHiddenElectionRepository
        extends JpaRepository<StudentHiddenElection, Long> {

    boolean existsByStudentIdAndElectionId(
            Long studentId,
            Long electionId
    );

    List<StudentHiddenElection> findByStudentId(Long studentId);
}