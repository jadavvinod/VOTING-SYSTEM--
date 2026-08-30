package com.college.voting.repository;

import com.college.voting.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByElectionId(Long electionId);
}
