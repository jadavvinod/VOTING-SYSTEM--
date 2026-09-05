package com.college.voting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_hidden_elections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_hidden_election",
                        columnNames = {"student_id", "election_id"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentElectionHidden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Column(nullable = false, updatable = false)
    private LocalDateTime hiddenAt;

    @PrePersist
    protected void onCreate() {
        this.hiddenAt = LocalDateTime.now();
    }
}