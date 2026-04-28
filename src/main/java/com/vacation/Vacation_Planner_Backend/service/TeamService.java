package com.vacation.Vacation_Planner_Backend.service;

import com.vacation.Vacation_Planner_Backend.dto.team.request.CreateTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.request.JoinTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.response.CreateTeamResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.JoinTeamResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.TeamCalendarResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.TeamMemberResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;

import java.util.List;

public interface TeamService {
    CreateTeamResponse createTeam(CreateTeamRequest request, User currentUser);
    JoinTeamResponse joinTeam(JoinTeamRequest request, User currentUser);
    List<TeamMemberResponse> getTeamMembers(User currentUser);
    List<TeamCalendarResponse> getTeamCalendar(User currentUser);
}
