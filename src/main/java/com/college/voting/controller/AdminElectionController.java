package com.college.voting.controller;

import com.college.voting.dto.ElectionRequest;
import com.college.voting.dto.ElectionResponse;
import com.college.voting.service.ElectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/elections")
@RequiredArgsConstructor
public class AdminElectionController {

    private final ElectionService electionService;

    @PostMapping
    public ResponseEntity<ElectionResponse> create(
            @Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(electionService.createElection(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ElectionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(
                electionService.updateElection(id, request));
    }

    @GetMapping
    public ResponseEntity<List<ElectionResponse>> listAll() {
        return ResponseEntity.ok(electionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectionResponse> get(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                electionService.getElection(id));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ElectionResponse> start(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                electionService.startElection(id));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ElectionResponse> close(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                electionService.closeElection(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ElectionResponse> cancel(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                electionService.cancelElection(id));
    }

    @DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    electionService.deleteElection(id);
    return ResponseEntity.noContent().build();
}
}