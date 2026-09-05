package com.college.voting.repository;

import com.college.voting.entity.StudentElectionHidden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentElectionHiddenRepository
        extends JpaRepository<StudentElectionHidden, Long> {

    boolean existsByStudentIdAndElectionId(
            Long studentId,
            Long electionId
    );

    List<StudentElectionHidden> findByStudentId(Long studentId);

    void deleteByStudentIdAndElectionId(
            Long studentId,
            Long electionId
    );
}