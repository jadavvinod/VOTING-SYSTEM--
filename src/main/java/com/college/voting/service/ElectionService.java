package com.college.voting.service;

import com.college.voting.dto.ElectionRequest;
import com.college.voting.dto.ElectionResponse;
import com.college.voting.entity.Election;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.entity.NotificationType;
import com.college.voting.entity.Student;
import com.college.voting.entity.StudentHiddenElection;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.ElectionRepository;
import com.college.voting.repository.StudentHiddenElectionRepository;
import com.college.voting.repository.StudentRepository;
import com.college.voting.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final StudentRepository studentRepository;
    private final StudentHiddenElectionRepository studentHiddenElectionRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ElectionResponse createElection(ElectionRequest request) {

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new IllegalArgumentException(
                    "End date/time must be after start date/time");
        }

        Election election = Election.builder()
                .name(request.getName())
                .description(request.getDescription())
                .academicYear(request.getAcademicYear())
                .department(request.getDepartment())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .rules(request.getRules())
                .showLiveResults(request.isShowLiveResults())
                .status(ElectionStatus.UPCOMING)
                .build();

        Election saved = electionRepository.save(election);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election created",
                "Election",
                saved.getId()
        );

        return ElectionResponse.fromEntity(saved);
    }

    @Transactional
    public ElectionResponse updateElection(
            Long electionId,
            ElectionRequest request) {

        Election election = getElectionEntity(electionId);

        if (election.getEffectiveStatus() != ElectionStatus.UPCOMING) {
            throw new IllegalStateException(
                    "Election can only be updated before it starts");
        }

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new IllegalArgumentException(
                    "End date/time must be after start date/time");
        }

        election.setName(request.getName());
        election.setDescription(request.getDescription());
        election.setAcademicYear(request.getAcademicYear());
        election.setDepartment(request.getDepartment());
        election.setStartDateTime(request.getStartDateTime());
        election.setEndDateTime(request.getEndDateTime());
        election.setRules(request.getRules());
        election.setShowLiveResults(request.isShowLiveResults());

        Election saved = electionRepository.save(election);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election updated",
                "Election",
                saved.getId()
        );

        return ElectionResponse.fromEntity(saved);
    }

    public ElectionResponse getElection(Long electionId) {
        return ElectionResponse.fromEntityWithPositions(
                getElectionEntity(electionId));
    }

    public Election getElectionEntity(Long electionId) {
        return electionRepository.findById(electionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Election not found with id: " + electionId));
    }

    public List<ElectionResponse> listAll() {
        return electionRepository
                .findAllByOrderByStartDateTimeDesc()
                .stream()
                .map(ElectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ElectionResponse> listByEffectiveStatus(
            ElectionStatus status) {

        return electionRepository
                .findAllByOrderByStartDateTimeDesc()
                .stream()
                .filter(e -> e.getEffectiveStatus() == status)
                .map(ElectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns elections applicable to a particular student.
     *
     * Department rules:
     *
     * 1. Election with no department -> available to everyone.
     * 2. Election with blank department -> available to everyone.
     * 3. Election with matching department -> available to that student.
     * 4. Election with different department -> hidden from that student.
     *
     * For completed elections, elections hidden by this student
     * are also excluded from the result.
     */
    public List<ElectionResponse> listByEffectiveStatusForStudent(
            ElectionStatus status,
            Student student) {

        List<Election> elections = electionRepository
                .findAllByOrderByStartDateTimeDesc()
                .stream()
                .filter(e -> e.getEffectiveStatus() == status)
                .filter(e ->
                        e.getDepartment() == null
                        || e.getDepartment().isBlank()
                        || e.getDepartment()
                                .equalsIgnoreCase(student.getDepartment())
                )
                .collect(Collectors.toList());

        if (status != ElectionStatus.COMPLETED) {
            return elections.stream()
                    .map(ElectionResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        Set<Long> hiddenElectionIds =
                studentHiddenElectionRepository
                        .findByStudentId(student.getId())
                        .stream()
                        .map(hidden -> hidden.getElection().getId())
                        .collect(Collectors.toSet());

        return elections.stream()
                .filter(e -> !hiddenElectionIds.contains(e.getId()))
                .map(ElectionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Hides a completed election only for the current student.
     *
     * The actual election, votes, candidates and results are NOT deleted.
     */
    @Transactional
    public void hideCompletedElection(
            Long electionId,
            Student student) {

        Election election = getElectionEntity(electionId);

        if (election.getEffectiveStatus() != ElectionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed elections can be removed from your list");
        }

        boolean alreadyHidden =
                studentHiddenElectionRepository
                        .existsByStudentIdAndElectionId(
                                student.getId(),
                                electionId
                        );

        if (alreadyHidden) {
            return;
        }

        StudentHiddenElection hiddenElection =
                StudentHiddenElection.builder()
                        .student(student)
                        .election(election)
                        .build();

        studentHiddenElectionRepository.save(hiddenElection);
    }

    @Transactional
    public ElectionResponse startElection(Long electionId) {

        Election election = getElectionEntity(electionId);

        if (election.getEffectiveStatus() != ElectionStatus.UPCOMING) {
            throw new IllegalStateException(
                    "Only upcoming elections can be started");
        }

        election.setStatus(ElectionStatus.ACTIVE);

        Election saved = electionRepository.save(election);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election started",
                "Election",
                saved.getId()
        );

        List<Student> allStudents = studentRepository.findAll();

        for (Student s : allStudents) {
            notificationService.notify(
                    s,
                    saved.getName() + " is now open",
                    "Voting has started for \"" +
                            saved.getName() +
                            "\". Cast your vote before it closes.",
                    NotificationType.ELECTION_OPEN
            );
        }

        return ElectionResponse.fromEntity(saved);
    }

    @Transactional
    public ElectionResponse closeElection(Long electionId) {

        Election election = getElectionEntity(electionId);

        if (election.getEffectiveStatus() == ElectionStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cancelled election cannot be closed");
        }

        election.setStatus(ElectionStatus.COMPLETED);

        Election saved = electionRepository.save(election);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election closed",
                "Election",
                saved.getId()
        );

        List<Student> allStudents = studentRepository.findAll();

        for (Student s : allStudents) {
            notificationService.notify(
                    s,
                    "Results available: " + saved.getName(),
                    "Voting has closed for \"" +
                            saved.getName() +
                            "\". Results are now available.",
                    NotificationType.RESULTS_PUBLISHED
            );
        }

        return ElectionResponse.fromEntity(saved);
    }

    @Transactional
    public ElectionResponse cancelElection(Long electionId) {

        Election election = getElectionEntity(electionId);

        if (election.getEffectiveStatus() == ElectionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed election cannot be cancelled");
        }

        election.setStatus(ElectionStatus.CANCELLED);

        Election saved = electionRepository.save(election);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election cancelled",
                "Election",
                saved.getId()
        );

        return ElectionResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteElection(Long electionId) {

        Election election = getElectionEntity(electionId);

        Long deletedId = election.getId();
        String deletedName = election.getName();

        // Delete records that reference this election first
        jdbcTemplate.update(
                "DELETE FROM student_hidden_elections WHERE election_id = ?",
                electionId
        );

        jdbcTemplate.update(
                "DELETE FROM votes WHERE election_id = ?",
                electionId
        );

        jdbcTemplate.update(
                "DELETE FROM candidates WHERE election_id = ?",
                electionId
        );

        jdbcTemplate.update(
                "DELETE FROM positions WHERE election_id = ?",
                electionId
        );

        // Finally delete the election
        jdbcTemplate.update(
                "DELETE FROM elections WHERE id = ?",
                electionId
        );

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election deleted: " + deletedName,
                "Election",
                deletedId
        );
    }
}