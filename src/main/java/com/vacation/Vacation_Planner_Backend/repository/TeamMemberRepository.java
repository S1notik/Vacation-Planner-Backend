package com.vacation.Vacation_Planner_Backend.repository;

import com.vacation.Vacation_Planner_Backend.model.entity.Team;
import com.vacation.Vacation_Planner_Backend.model.entity.TeamMember;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeam(Team team);
    boolean existsByTeamAndUser(Team team, User user);
    List<TeamMember> findByUser(User user);
}
