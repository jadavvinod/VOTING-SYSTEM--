package com.college.voting.controller;

import com.college.voting.dto.ElectionResultResponse;
import com.college.voting.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ResultService resultService;

    @GetMapping("/{electionId}")
    public ElectionResultResponse getAnalytics(
            @PathVariable Long electionId) {

        return resultService.calculateResults(electionId);
    }
}