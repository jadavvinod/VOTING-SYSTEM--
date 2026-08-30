package com.college.voting.service;

import com.college.voting.dto.CandidateResultResponse;
import com.college.voting.dto.ElectionResultResponse;
import com.college.voting.dto.PositionResultResponse;
import com.college.voting.entity.Candidate;
import com.college.voting.entity.CandidateStatus;
import com.college.voting.entity.Election;
import com.college.voting.entity.Position;
import com.college.voting.repository.CandidateRepository;
import com.college.voting.repository.PositionRepository;
import com.college.voting.repository.StudentRepository;
import com.college.voting.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final VoteRepository voteRepository;
    private final PositionRepository positionRepository;
    private final CandidateRepository candidateRepository;
    private final StudentRepository studentRepository;
    private final ElectionService electionService;

    /**
     * Computes official results server-side. The frontend only ever displays what
     * this method returns — it never calculates percentages, winners, or ties itself.
     */
    public ElectionResultResponse calculateResults(Long electionId) {
        Election election = electionService.getElectionEntity(electionId);
        List<Position> positions = positionRepository.findByElectionId(electionId);

        List<PositionResultResponse> positionResults = positions.stream()
                .map(this::calculatePositionResult)
                .collect(Collectors.toList());

        long totalEligible = studentRepository.count();
        long totalVoters = voteRepository.countDistinctVotersByElectionId(electionId);
        double turnout = totalEligible == 0 ? 0.0 : (totalVoters * 100.0) / totalEligible;

        return ElectionResultResponse.builder()
                .electionId(election.getId())
                .electionName(election.getName())
                .status(election.getEffectiveStatus().name())
                .totalEligibleStudents(totalEligible)
                .totalVoters(totalVoters)
                .turnoutPercentage(round(turnout))
                .positions(positionResults)
                .build();
    }

    private PositionResultResponse calculatePositionResult(Position position) {
        List<Candidate> approvedCandidates =
                candidateRepository.findByPositionIdAndStatus(position.getId(), CandidateStatus.APPROVED);

        long totalVotesForPosition = approvedCandidates.stream()
                .mapToLong(c -> voteRepository.countByPositionIdAndCandidateId(position.getId(), c.getId()))
                .sum();

        long maxVotes = approvedCandidates.stream()
                .mapToLong(c -> voteRepository.countByPositionIdAndCandidateId(position.getId(), c.getId()))
                .max()
                .orElse(0);

        long leadersCount = approvedCandidates.stream()
                .filter(c -> voteRepository.countByPositionIdAndCandidateId(position.getId(), c.getId()) == maxVotes)
                .count();

        boolean isTie = maxVotes > 0 && leadersCount > 1;

        List<CandidateResultResponse> candidateResults = approvedCandidates.stream()
                .map(c -> {
                    long votes = voteRepository.countByPositionIdAndCandidateId(position.getId(), c.getId());
                    double pct = totalVotesForPosition == 0 ? 0.0 : (votes * 100.0) / totalVotesForPosition;
                    boolean winner = !isTie && votes == maxVotes && votes > 0;
                    return CandidateResultResponse.builder()
                            .candidateId(c.getId())
                            .candidateName(c.getStudent().getFullName())
                            .votes(votes)
                            .percentage(round(pct))
                            .winner(winner)
                            .build();
                })
                .sorted(Comparator.comparingLong(CandidateResultResponse::getVotes).reversed())
                .collect(Collectors.toList());

        return PositionResultResponse.builder()
                .positionId(position.getId())
                .positionTitle(position.getTitle())
                .totalVotes(totalVotesForPosition)
                .isTie(isTie)
                .candidates(candidateResults)
                .build();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public String resultsToCsv(Long electionId) {
        ElectionResultResponse results = calculateResults(electionId);
        StringBuilder sb = new StringBuilder("position,candidate,votes,percentage,winner\n");
        for (PositionResultResponse pos : results.getPositions()) {
            for (CandidateResultResponse c : pos.getCandidates()) {
                sb.append(csvSafe(pos.getPositionTitle())).append(",")
                  .append(csvSafe(c.getCandidateName())).append(",")
                  .append(c.getVotes()).append(",")
                  .append(c.getPercentage()).append(",")
                  .append(c.isWinner() ? "WINNER" : (pos.isTie() ? "TIE" : ""))
                  .append("\n");
            }
        }
        return sb.toString();
    }

    private String csvSafe(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
