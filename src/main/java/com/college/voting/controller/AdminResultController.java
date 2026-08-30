package com.college.voting.controller;

import com.college.voting.dto.ElectionResultResponse;
import com.college.voting.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/elections")
@RequiredArgsConstructor
public class AdminResultController {

    private final ResultService resultService;

    @GetMapping("/{electionId}/results")
    public ElectionResultResponse results(@PathVariable Long electionId) {
        return resultService.calculateResults(electionId);
    }

    @GetMapping("/{electionId}/results/export")
    public ResponseEntity<byte[]> exportResults(@PathVariable Long electionId) {
        String csv = resultService.resultsToCsv(electionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"election-" + electionId + "-results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
