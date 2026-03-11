package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.dto.response.NotificationResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(currentUser));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                Map.of("nonLues", notificationService.countUnread(currentUser)));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllRead(
            @AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(Map.of("message", "Notifications marquees comme lues"));
    }
}