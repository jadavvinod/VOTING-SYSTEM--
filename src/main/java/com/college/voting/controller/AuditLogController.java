package com.college.voting.controller;

import com.college.voting.dto.AuditLogResponse;
import com.college.voting.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogResponse> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType) {
        if ((action == null || action.isBlank()) && (entityType == null || entityType.isBlank())) {
            return auditLogService.listAll();
        }
        return auditLogService.search(action, entityType);
    }
}
