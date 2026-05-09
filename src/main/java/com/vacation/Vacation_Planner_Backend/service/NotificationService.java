package com.vacation.Vacation_Planner_Backend.service;

import com.vacation.Vacation_Planner_Backend.dto.notification.response.NotificationResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationResponse> getNotifications(User currentUser);
    void markAsRead(UUID notificationId, User currentUser);
    void markAllAsRead(User currentUser);
}