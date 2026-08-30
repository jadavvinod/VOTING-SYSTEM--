package com.college.voting.controller;

import com.college.voting.dto.PositionRequest;
import com.college.voting.dto.PositionResponse;
import com.college.voting.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/elections/{electionId}/positions")
@RequiredArgsConstructor
public class AdminPositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<PositionResponse> add(@PathVariable Long electionId, @Valid @RequestBody PositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.addPosition(electionId, request));
    }

    @PutMapping("/{positionId}")
    public ResponseEntity<PositionResponse> update(@PathVariable Long electionId, @PathVariable Long positionId,
                                                     @Valid @RequestBody PositionRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(positionId, request));
    }

    @DeleteMapping("/{positionId}")
    public ResponseEntity<Void> delete(@PathVariable Long electionId, @PathVariable Long positionId) {
        positionService.deletePosition(positionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PositionResponse>> list(@PathVariable Long electionId) {
        return ResponseEntity.ok(positionService.listByElection(electionId));
    }
}
