package com.college.voting.repository;

import com.college.voting.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByTimestampDesc();

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :action, '%'))) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> search(@Param("action") String action, @Param("entityType") String entityType);
}
