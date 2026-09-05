package com.college.voting.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "elections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Election {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 20)
    private String academicYear;

    // Department / Branch
    // Empty / ALL means election is available to all departments.
    @Column(length = 100)
    private String department;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(length = 3000)
    private String rules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ElectionStatus status = ElectionStatus.UPCOMING;

    @Column(nullable = false)
    @Builder.Default
    private boolean showLiveResults = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "election",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Position> positions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Returns the actual/current election status.
     *
     * IMPORTANT:
     * - CANCELLED must always remain CANCELLED.
     * - COMPLETED must always remain COMPLETED.
     * - ACTIVE must remain ACTIVE until end time.
     * - UPCOMING automatically becomes ACTIVE when start time arrives.
     * - ACTIVE automatically becomes COMPLETED after end time.
     */
    @Transient
    public ElectionStatus getEffectiveStatus() {

        LocalDateTime now = LocalDateTime.now();

        // -----------------------------------------
        // 1. Manually cancelled election
        // -----------------------------------------
        if (status == ElectionStatus.CANCELLED) {
            return ElectionStatus.CANCELLED;
        }

        // -----------------------------------------
        // 2. Manually closed/completed election
        // -----------------------------------------
        if (status == ElectionStatus.COMPLETED) {
            return ElectionStatus.COMPLETED;
        }

        // -----------------------------------------
        // 3. Manually started election
        // -----------------------------------------
        if (status == ElectionStatus.ACTIVE) {

            // If end time has passed,
            // automatically mark it as completed.
            if (now.isAfter(endDateTime)) {
                return ElectionStatus.COMPLETED;
            }

            return ElectionStatus.ACTIVE;
        }

        // -----------------------------------------
        // 4. UPCOMING election
        // -----------------------------------------
        if (now.isBefore(startDateTime)) {
            return ElectionStatus.UPCOMING;
        }

        // -----------------------------------------
        // 5. Scheduled time reached
        // -----------------------------------------
        if (now.isAfter(endDateTime)) {
            return ElectionStatus.COMPLETED;
        }

        // -----------------------------------------
        // 6. Current time is between start & end
        // -----------------------------------------
        return ElectionStatus.ACTIVE;
    }
}