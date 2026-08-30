package com.college.voting.dto;

import com.college.voting.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String adminName;
    private String action;
    private String entityType;
    private Long entityId;
    private LocalDateTime timestamp;

    public static AuditLogResponse fromEntity(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .adminName(a.getAdmin() != null ? a.getAdmin().getFullName() : "System")
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .timestamp(a.getTimestamp())
                .build();
    }
}
