package com.vacation.Vacation_Planner_Backend.model.entity;

import com.vacation.Vacation_Planner_Backend.model.enums.EntityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "google_calendar_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_gct_user_entity",
                columnNames = {"user_id", "entity_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCalendarToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * EMPLOYER - токен корпоративного (командного) календаря
     * EMPLOYEE - токен личного календаря сотрудника
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "access_token", nullable = false, columnDefinition = "text")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "text")
    private String refreshToken;

    @Column(name = "calendar_id", length = 255)
    private String calendarId;

    @CreationTimestamp
    @Column(name = "connected_at", updatable = false)
    private LocalDateTime connectedAt;
}
