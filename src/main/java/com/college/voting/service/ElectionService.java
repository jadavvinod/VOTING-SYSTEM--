package com.college.voting.service;

import com.college.voting.dto.ElectionRequest;
import com.college.voting.dto.ElectionResponse;
import com.college.voting.entity.Election;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.entity.NotificationType;
import com.college.voting.entity.Student;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.ElectionRepository;
import com.college.voting.repository.StudentRepository;
import com.college.voting.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final StudentRepository studentRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

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

        electionRepository.delete(election);

        electionRepository.flush();

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Election deleted: " + deletedName,
                "Election",
                deletedId
        );
    }
}