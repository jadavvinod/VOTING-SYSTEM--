package com.college.voting.controller;

import com.college.voting.dto.CandidateRequest;
import com.college.voting.dto.CandidateResponse;
import com.college.voting.service.CandidateService;
import com.college.voting.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/candidates")
@RequiredArgsConstructor
public class AdminCandidateController {

    private final CandidateService candidateService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<CandidateResponse> add(@Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.addCandidate(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidate(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidate(id));
    }

    @GetMapping("/election/{electionId}")
    public ResponseEntity<List<CandidateResponse>> listByElection(@PathVariable Long electionId) {
        return ResponseEntity.ok(candidateService.listByElection(electionId));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<CandidateResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.approveCandidate(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<CandidateResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.rejectCandidate(id));
    }

    @PatchMapping("/{id}/disqualify")
    public ResponseEntity<CandidateResponse> disqualify(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.disqualifyCandidate(id));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String photoUrl = fileStorageService.storeCandidatePhoto(file);
        candidateService.updatePhotoUrl(id, photoUrl);
        return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
    }
}
