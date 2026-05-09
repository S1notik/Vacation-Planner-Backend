package com.vacation.Vacation_Planner_Backend.controller;

import com.vacation.Vacation_Planner_Backend.dto.team.request.CreateTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.request.JoinTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.response.CreateTeamResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.JoinTeamResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.TeamCalendarResponse;
import com.vacation.Vacation_Planner_Backend.dto.team.response.TeamMemberResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // Get current authenticated user from JWT
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // Employer creates a team
    @PostMapping
    public ResponseEntity<CreateTeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.ok(teamService.createTeam(request, getCurrentUser()));
    }

    // Employee joins a team via invite code
    @PostMapping("/join")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<JoinTeamResponse> joinTeam(
            @Valid @RequestBody JoinTeamRequest request) {
        return ResponseEntity.ok(teamService.joinTeam(request, getCurrentUser()));
    }

    // Get all team members
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers() {
        return ResponseEntity.ok(teamService.getTeamMembers(getCurrentUser()));
    }

    // Get team vacation calendar
    @GetMapping("/calendar")
    public ResponseEntity<List<TeamCalendarResponse>> getTeamCalendar() {
        return ResponseEntity.ok(teamService.getTeamCalendar(getCurrentUser()));
    }
}