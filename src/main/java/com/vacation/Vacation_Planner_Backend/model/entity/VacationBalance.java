package com.vacation.Vacation_Planner_Backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "vacation_balance",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_vacation_balance_user_year",
                columnNames = {"user_id", "year"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_days", nullable = false)
    @Builder.Default
    private Integer totalDays = 28;

    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private Integer usedDays = 0;


    public int remainingDays() {
        return totalDays - usedDays;
    }

    public boolean hasEnoughDays(int requested) {
        return remainingDays() >= requested;
    }
}