package com.college.voting.repository;

import com.college.voting.entity.Election;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElectionRepository extends JpaRepository<Election, Long> {
    List<Election> findAllByOrderByStartDateTimeDesc();
}
