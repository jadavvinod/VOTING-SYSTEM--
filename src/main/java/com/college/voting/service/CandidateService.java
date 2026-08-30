package com.college.voting.service;

import com.college.voting.dto.CandidateRequest;
import com.college.voting.dto.CandidateResponse;
import com.college.voting.entity.Candidate;
import com.college.voting.entity.CandidateStatus;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.entity.Position;
import com.college.voting.entity.Student;
import com.college.voting.exception.DuplicateResourceException;
import com.college.voting.exception.ElectionNotActiveException;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.CandidateRepository;
import com.college.voting.repository.StudentRepository;
import com.college.voting.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final StudentRepository studentRepository;
    private final PositionService positionService;
    private final AuditLogService auditLogService;
    private final SecurityUtils securityUtils;

    @Transactional
    public CandidateResponse addCandidate(CandidateRequest request) {

        Student student = studentRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student found with Student ID: " + request.getStudentId()));

        Position position = positionService.getPositionEntity(request.getPositionId());

        ElectionStatus status = position.getElection().getEffectiveStatus();

        if (status != ElectionStatus.UPCOMING) {
            throw new ElectionNotActiveException(
                    "Candidates cannot be added after the election has started.");
        }

        if (candidateRepository.existsByStudentIdAndPositionId(
                student.getId(), position.getId())) {

            throw new DuplicateResourceException(
                    "This student is already a candidate for this position");
        }

        Candidate candidate = Candidate.builder()
                .student(student)
                .election(position.getElection())
                .position(position)
                .manifesto(request.getManifesto())
                .slogan(request.getSlogan())
                .photoUrl(request.getPhotoUrl())
                .status(CandidateStatus.PENDING)
                .build();

        Candidate saved = candidateRepository.save(candidate);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Candidate created",
                "Candidate",
                saved.getId()
        );

        return CandidateResponse.fromEntity(saved);
    }

    @Transactional
    public CandidateResponse updateCandidate(
            Long candidateId,
            CandidateRequest request) {

        Candidate candidate = getCandidateEntity(candidateId);

        if (candidate.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new ElectionNotActiveException(
                    "Candidates cannot be modified after the election has started.");
        }

        candidate.setManifesto(request.getManifesto());
        candidate.setSlogan(request.getSlogan());

        if (request.getPhotoUrl() != null
                && !request.getPhotoUrl().isBlank()) {

            candidate.setPhotoUrl(request.getPhotoUrl());
        }

        return CandidateResponse.fromEntity(
                candidateRepository.save(candidate));
    }

    @Transactional
    public void updatePhotoUrl(Long candidateId, String photoUrl) {

        Candidate candidate = getCandidateEntity(candidateId);

        if (candidate.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new ElectionNotActiveException(
                    "Candidate photo cannot be changed after the election has started.");
        }

        candidate.setPhotoUrl(photoUrl);
        candidateRepository.save(candidate);
    }

    @Transactional
    public CandidateResponse approveCandidate(Long candidateId) {

        Candidate candidate = getCandidateEntity(candidateId);

        if (candidate.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new ElectionNotActiveException(
                    "Candidates cannot be approved after the election has started.");
        }

        candidate.setStatus(CandidateStatus.APPROVED);

        Candidate saved = candidateRepository.save(candidate);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Candidate approved",
                "Candidate",
                saved.getId()
        );

        return CandidateResponse.fromEntity(saved);
    }

    @Transactional
    public CandidateResponse rejectCandidate(Long candidateId) {

        Candidate candidate = getCandidateEntity(candidateId);

        if (candidate.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new ElectionNotActiveException(
                    "Candidates cannot be rejected after the election has started.");
        }

        candidate.setStatus(CandidateStatus.REJECTED);

        Candidate saved = candidateRepository.save(candidate);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Candidate rejected",
                "Candidate",
                saved.getId()
        );

        return CandidateResponse.fromEntity(saved);
    }

    @Transactional
    public CandidateResponse disqualifyCandidate(Long candidateId) {

        Candidate candidate = getCandidateEntity(candidateId);

        if (candidate.getElection().getEffectiveStatus()
                == ElectionStatus.COMPLETED) {

            throw new ElectionNotActiveException(
                    "Candidate cannot be disqualified after the election has ended.");
        }

        candidate.setStatus(CandidateStatus.DISQUALIFIED);

        Candidate saved = candidateRepository.save(candidate);

        auditLogService.log(
                securityUtils.getCurrentAdmin(),
                "Candidate disqualified",
                "Candidate",
                saved.getId()
        );

        return CandidateResponse.fromEntity(saved);
    }

    public CandidateResponse getCandidate(Long candidateId) {
        return CandidateResponse.fromEntity(
                getCandidateEntity(candidateId));
    }

    public Candidate getCandidateEntity(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with id: " + candidateId));
    }

    public List<CandidateResponse> listByElection(Long electionId) {

        return candidateRepository.findByElectionId(electionId)
                .stream()
                .map(CandidateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CandidateResponse> listApprovedByElection(Long electionId) {

        return candidateRepository
                .findByElectionIdAndStatus(
                        electionId,
                        CandidateStatus.APPROVED)
                .stream()
                .map(CandidateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CandidateResponse> listApprovedByPosition(Long positionId) {

        return candidateRepository
                .findByPositionIdAndStatus(
                        positionId,
                        CandidateStatus.APPROVED)
                .stream()
                .map(CandidateResponse::fromEntity)
                .collect(Collectors.toList());
    }
}