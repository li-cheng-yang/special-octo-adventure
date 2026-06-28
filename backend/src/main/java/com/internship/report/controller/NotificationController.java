package com.internship.report.controller;

import com.internship.report.dto.ApiResponse;
import com.internship.report.dto.NotificationDTO;
import com.internship.report.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationController.PageResponse<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = notificationService.getNotifications(userId, page, size);
        PageResponse<NotificationDTO> response = new PageResponse<>(
                result.getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getLatestNotifications(
            @AuthenticationPrincipal Long userId) {
        List<NotificationDTO> list = notificationService.getLatestNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("已全部标记为已读", null));
    }

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int currentPage) {}
}
