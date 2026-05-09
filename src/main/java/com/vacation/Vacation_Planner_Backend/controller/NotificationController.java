package com.vacation.Vacation_Planner_Backend.controller;

import com.vacation.Vacation_Planner_Backend.dto.notification.response.NotificationResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // Get all notifications
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.getNotifications(getCurrentUser()));
    }

    // Mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id, getCurrentUser());
        return ResponseEntity.ok().build();
    }

    // Mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUser());
        return ResponseEntity.ok().build();
    }
}
