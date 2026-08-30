package com.college.voting.service;

import com.college.voting.dto.NotificationResponse;
import com.college.voting.entity.Notification;
import com.college.voting.entity.NotificationType;
import com.college.voting.entity.Student;
import com.college.voting.exception.ResourceNotFoundException;
import com.college.voting.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(Student student, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .student(student)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> listForStudent(Long studentId) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public long unreadCount(Long studentId) {
        return notificationRepository.countByStudentIdAndIsReadFalse(studentId);
    }

    @Transactional
    public void markRead(Long notificationId, Long studentId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
