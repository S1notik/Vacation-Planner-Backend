package com.vacation.Vacation_Planner_Backend.repository;


import com.vacation.Vacation_Planner_Backend.model.entity.VacationBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VacationBalanceRepository extends JpaRepository<VacationBalance, UUID> {
}
