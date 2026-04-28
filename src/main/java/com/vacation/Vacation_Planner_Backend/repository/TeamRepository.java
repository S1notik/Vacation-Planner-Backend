package com.vacation.Vacation_Planner_Backend.repository;

import com.vacation.Vacation_Planner_Backend.model.entity.Team;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository  extends JpaRepository<Team, UUID> {
    Optional<Team> findByInviteCode(String inviteCode);
    Optional<Team> findByEmployer(User employer);
}
