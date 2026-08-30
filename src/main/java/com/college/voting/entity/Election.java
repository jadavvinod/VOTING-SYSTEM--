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

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Position> positions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Computes the real-time status based on start/end times, independent of the
     * admin-set status field. CANCELLED always wins. This is what voting eligibility
     * checks must use in Phase 3 — never trust the stored status alone.
     */
    @Transient
    public ElectionStatus getEffectiveStatus() {
        if (status == ElectionStatus.CANCELLED) {
            return ElectionStatus.CANCELLED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDateTime)) {
            return ElectionStatus.UPCOMING;
        } else if (now.isAfter(endDateTime)) {
            return ElectionStatus.COMPLETED;
        } else {
            return ElectionStatus.ACTIVE;
        }
    }
}
