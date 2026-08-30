package com.college.voting.service;

import com.college.voting.dto.DashboardStatsResponse;
import com.college.voting.entity.Election;
import com.college.voting.entity.ElectionStatus;
import com.college.voting.repository.CandidateRepository;
import com.college.voting.repository.ElectionRepository;
import com.college.voting.repository.StudentRepository;
import com.college.voting.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;

    public DashboardStatsResponse getStats() {
        long totalStudents = studentRepository.count();
        long totalCandidates = candidateRepository.count();

        List<Election> elections = electionRepository.findAll();
        long totalElections = elections.size();
        long activeElections = elections.stream()
                .filter(e -> e.getEffectiveStatus() == ElectionStatus.ACTIVE)
                .count();

        long totalVotesCast = elections.stream()
                .mapToLong(e -> voteRepository.countByElectionId(e.getId()))
                .sum();

        // Department/year turnout breakdown is computed against whichever election is
        // currently ACTIVE (most relevant to "right now"); falls back to the most
        // recently started election if none are active, or empty maps if there are none.
        Election referenceElection = elections.stream()
                .filter(e -> e.getEffectiveStatus() == ElectionStatus.ACTIVE)
                .findFirst()
                .orElse(elections.stream()
                        .max((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()))
                        .orElse(null));

        Map<String, Long> departmentTurnout = new LinkedHashMap<>();
        Map<String, Long> yearTurnout = new LinkedHashMap<>();
        double overallTurnout = 0.0;

        if (referenceElection != null) {
            long distinctVoters = voteRepository.countDistinctVotersByElectionId(referenceElection.getId());
            overallTurnout = totalStudents == 0 ? 0.0 : (distinctVoters * 100.0) / totalStudents;

            for (Object[] row : voteRepository.countDistinctVotersByDepartment(referenceElection.getId())) {
                String dept = row[0] != null ? row[0].toString() : "Unspecified";
                departmentTurnout.put(dept, (Long) row[1]);
            }
            for (Object[] row : voteRepository.countDistinctVotersByYear(referenceElection.getId())) {
                String year = row[0] != null ? row[0].toString() : "Unspecified";
                yearTurnout.put(year, (Long) row[1]);
            }
        }

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalCandidates(totalCandidates)
                .totalElections(totalElections)
                .activeElections(activeElections)
                .totalVotesCast(totalVotesCast)
                .overallTurnoutPercentage(Math.round(overallTurnout * 10.0) / 10.0)
                .departmentTurnout(departmentTurnout)
                .yearTurnout(yearTurnout)
                .build();
    }
}
