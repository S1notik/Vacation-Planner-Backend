package com.vacation.Vacation_Planner_Backend.repository;


import com.vacation.Vacation_Planner_Backend.model.entity.Notification;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
}
