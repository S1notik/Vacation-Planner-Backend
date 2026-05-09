package com.vacation.Vacation_Planner_Backend.repository;


import com.vacation.Vacation_Planner_Backend.model.entity.Team;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.model.entity.VacationRequest;
import com.vacation.Vacation_Planner_Backend.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, UUID> {
    List<VacationRequest> findByUserAndStatus(User user, Status status);
    List<VacationRequest> findByUser(User user);
    List<VacationRequest> findByTeam(Team team);
}
