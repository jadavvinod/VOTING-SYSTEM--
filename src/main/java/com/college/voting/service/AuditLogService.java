package com.college.voting.service;

import com.college.voting.dto.AuditLogResponse;
import com.college.voting.entity.Admin;
import com.college.voting.entity.AuditLog;
import com.college.voting.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Admin admin, String action, String entityType, Long entityId) {
        if (admin == null) {
            return; // No authenticated admin in context (e.g. system-seeded action) — skip silently.
        }
        AuditLog entry = AuditLog.builder()
                .admin(admin)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .build();
        auditLogRepository.save(entry);
    }

    public List<AuditLogResponse> search(String action, String entityType) {
        return auditLogRepository.search(
                        (action == null || action.isBlank()) ? null : action,
                        (entityType == null || entityType.isBlank()) ? null : entityType
                ).stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> listAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
