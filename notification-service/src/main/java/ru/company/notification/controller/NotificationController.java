package ru.company.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import ru.company.common.response.ApiResponse;
import ru.company.notification.entity.Notification;
import ru.company.notification.entity.NotificationStatus;
import ru.company.notification.repository.NotificationRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    @Operation(summary = "Get notification history")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(notificationRepository.findAll(PageRequest.of(page, size))));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for a user")
    public ResponseEntity<ApiResponse<Page<Notification>>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationRepository.findByUserId(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSent", notificationRepository.countByStatus(NotificationStatus.SENT));
        stats.put("totalFailed", notificationRepository.countByStatus(NotificationStatus.FAILED));
        stats.put("totalPending", notificationRepository.countByStatus(NotificationStatus.PENDING));
        stats.put("totalRetry", notificationRepository.countByStatus(NotificationStatus.RETRY));

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}