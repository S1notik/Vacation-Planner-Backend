package com.vacation.Vacation_Planner_Backend.dto.notification.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String type;
    private String message;
    private boolean isRead;
    private String createdAt;
}
