package com.college.voting.service;

import com.college.voting.dto.CsvImportSummaryResponse;
import com.college.voting.dto.StudentManagementRequest;
import com.college.voting.dto.StudentResponse;
import com.college.voting.entity.Student;
import com.college.voting.exception.DuplicateResourceException;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.StudentRepository;
import com.college.voting.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentManagementService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final SecurityUtils securityUtils;

    private static final String DEFAULT_IMPORT_PASSWORD = "Student@123";

    public List<StudentResponse> search(String query, String department, String course, String year) {
        return studentRepository.findAll().stream()
                .filter(s -> query == null || query.isBlank()
                        || s.getFullName().toLowerCase().contains(query.toLowerCase())
                        || s.getStudentId().toLowerCase().contains(query.toLowerCase())
                        || s.getEmail().toLowerCase().contains(query.toLowerCase()))
                .filter(s -> department == null || department.isBlank() || department.equalsIgnoreCase(s.getDepartment()))
                .filter(s -> course == null || course.isBlank() || course.equalsIgnoreCase(s.getCourse()))
                .filter(s -> year == null || year.isBlank() || year.equalsIgnoreCase(s.getYear()))
                .map(StudentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentResponse addStudent(StudentManagementRequest request) {
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("Student ID is already registered");
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_IMPORT_PASSWORD))
                .department(request.getDepartment())
                .course(request.getCourse())
                .year(request.getYear())
                .phoneNumber(request.getPhoneNumber())
                .status(Student.AccountStatus.ACTIVE)
                .build();

        Student saved = studentRepository.save(student);
        auditLogService.log(securityUtils.getCurrentAdmin(), "Student created", "Student", saved.getId());
        return StudentResponse.fromEntity(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentManagementRequest request) {
        Student student = getStudentEntity(id);

        if (!student.getEmail().equalsIgnoreCase(request.getEmail())
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        // Student ID is intentionally not editable here, per spec: "Student ID cannot be changed by student."
        // Admin edits are limited to contact/academic details.
        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setDepartment(request.getDepartment());
        student.setCourse(request.getCourse());
        student.setYear(request.getYear());
        student.setPhoneNumber(request.getPhoneNumber());

        Student saved = studentRepository.save(student);
        auditLogService.log(securityUtils.getCurrentAdmin(), "Student updated", "Student", saved.getId());
        return StudentResponse.fromEntity(saved);
    }

    @Transactional
    public StudentResponse activateStudent(Long id) {
        Student student = getStudentEntity(id);
        student.setStatus(Student.AccountStatus.ACTIVE);
        Student saved = studentRepository.save(student);
        auditLogService.log(securityUtils.getCurrentAdmin(), "Student activated", "Student", saved.getId());
        return StudentResponse.fromEntity(saved);
    }

    @Transactional
    public StudentResponse deactivateStudent(Long id) {
        Student student = getStudentEntity(id);
        student.setStatus(Student.AccountStatus.DISABLED);
        Student saved = studentRepository.save(student);
        auditLogService.log(securityUtils.getCurrentAdmin(), "Student disabled", "Student", saved.getId());
        return StudentResponse.fromEntity(saved);
    }

    public Student getStudentEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    /**
     * Imports students from a CSV with header: student_id,name,email,department,course,year
     * Every imported student gets the same default password (communicate it to students
     * separately) — they should change it after first login.
     */
    @Transactional
    public CsvImportSummaryResponse importCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int total = 0, successful = 0, failed = 0, duplicates = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine(); // skip header
            String line;
            int rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                total++;

                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    failed++;
                    errors.add("Row " + rowNum + ": expected 6 columns (student_id,name,email,department,course,year), got " + parts.length);
                    continue;
                }

                String studentId = parts[0].trim();
                String name = parts[1].trim();
                String email = parts[2].trim();
                String department = parts[3].trim();
                String course = parts[4].trim();
                String year = parts[5].trim();

                if (studentId.isEmpty() || name.isEmpty() || email.isEmpty()) {
                    failed++;
                    errors.add("Row " + rowNum + ": missing required field(s)");
                    continue;
                }

                if (studentRepository.existsByStudentId(studentId) || studentRepository.existsByEmail(email)) {
                    duplicates++;
                    errors.add("Row " + rowNum + ": duplicate student ID or email (" + studentId + ")");
                    continue;
                }

                try {
                    Student student = Student.builder()
                            .studentId(studentId)
                            .fullName(name)
                            .email(email)
                            .password(passwordEncoder.encode(DEFAULT_IMPORT_PASSWORD))
                            .department(department)
                            .course(course)
                            .year(year)
                            .status(Student.AccountStatus.ACTIVE)
                            .build();
                    studentRepository.save(student);
                    successful++;
                } catch (Exception ex) {
                    failed++;
                    errors.add("Row " + rowNum + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read CSV file: " + e.getMessage());
        }

        auditLogService.log(securityUtils.getCurrentAdmin(), "Students imported via CSV", "Student", null);

        return CsvImportSummaryResponse.builder()
                .totalRecords(total)
                .successful(successful)
                .failed(failed)
                .duplicates(duplicates)
                .errors(errors)
                .defaultPasswordNote("Imported students were assigned the default password '" + DEFAULT_IMPORT_PASSWORD + "' — ask them to change it after first login.")
                .build();
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("student_id,name,email,department,course,year,status\n");
        for (Student s : studentRepository.findAll()) {
            sb.append(csvSafe(s.getStudentId())).append(",")
              .append(csvSafe(s.getFullName())).append(",")
              .append(csvSafe(s.getEmail())).append(",")
              .append(csvSafe(s.getDepartment())).append(",")
              .append(csvSafe(s.getCourse())).append(",")
              .append(csvSafe(s.getYear())).append(",")
              .append(s.getStatus().name())
              .append("\n");
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
