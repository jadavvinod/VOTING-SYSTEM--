package com.college.voting.controller;

import com.college.voting.dto.CsvImportSummaryResponse;
import com.college.voting.dto.StudentManagementRequest;
import com.college.voting.dto.StudentResponse;
import com.college.voting.service.StudentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final StudentManagementService studentManagementService;

    @GetMapping
    public List<StudentResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String year) {
        return studentManagementService.search(query, department, course, year);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> add(@Valid @RequestBody StudentManagementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentManagementService.addStudent(request));
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentManagementRequest request) {
        return studentManagementService.updateStudent(id, request);
    }

    @PatchMapping("/{id}/activate")
    public StudentResponse activate(@PathVariable Long id) {
        return studentManagementService.activateStudent(id);
    }

    @PatchMapping("/{id}/deactivate")
    public StudentResponse deactivate(@PathVariable Long id) {
        return studentManagementService.deactivateStudent(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportSummaryResponse importCsv(@RequestParam("file") MultipartFile file) {
        return studentManagementService.importCsv(file);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = studentManagementService.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
