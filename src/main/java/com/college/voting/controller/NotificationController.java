package com.college.voting.controller;

import com.college.voting.dto.NotificationResponse;
import com.college.voting.entity.Student;
import com.college.voting.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal Student student) {
        return notificationService.listForStudent(student.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Student student) {
        return Map.of("count", notificationService.unreadCount(student.getId()));
    }

    @PatchMapping("/{id}/read")
    public void markRead(@AuthenticationPrincipal Student student, @PathVariable Long id) {
        notificationService.markRead(id, student.getId());
    }
}
