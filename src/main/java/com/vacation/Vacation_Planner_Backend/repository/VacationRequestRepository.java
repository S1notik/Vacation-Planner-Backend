package com.vacation.Vacation_Planner_Backend.repository;


import com.vacation.Vacation_Planner_Backend.model.entity.VacationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, UUID> {
}
