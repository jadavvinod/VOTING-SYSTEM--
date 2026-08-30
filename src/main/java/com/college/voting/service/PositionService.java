package com.college.voting.service;

import com.college.voting.dto.PositionRequest;
import com.college.voting.dto.PositionResponse;
import com.college.voting.entity.Election;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.entity.Position;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;
    private final ElectionService electionService;

    @Transactional
    public PositionResponse addPosition(Long electionId, PositionRequest request) {
        Election election = electionService.getElectionEntity(electionId);

        if (election.getEffectiveStatus() != ElectionStatus.UPCOMING) {
            throw new IllegalStateException(
                    "Positions can only be added before the election starts");
        }

        Position position = Position.builder()
                .election(election)
                .title(request.getTitle())
                .build();

        return PositionResponse.fromEntity(
                positionRepository.save(position));
    }

    @Transactional
    public PositionResponse updatePosition(
            Long positionId,
            PositionRequest request) {

        Position position = getPositionEntity(positionId);

        if (position.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new IllegalStateException(
                    "Positions can only be updated before the election starts");
        }

        position.setTitle(request.getTitle());

        return PositionResponse.fromEntity(
                positionRepository.save(position));
    }

    @Transactional
    public void deletePosition(Long positionId) {

        Position position = getPositionEntity(positionId);

        if (position.getElection().getEffectiveStatus()
                != ElectionStatus.UPCOMING) {

            throw new IllegalStateException(
                    "Positions can only be deleted before the election starts");
        }

        positionRepository.delete(position);
    }

    public List<PositionResponse> listByElection(Long electionId) {

        return positionRepository.findByElectionId(electionId)
                .stream()
                .map(PositionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Position getPositionEntity(Long positionId) {

        return positionRepository.findById(positionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Position not found with id: " + positionId));
    }
}