package com.vacation.Vacation_Planner_Backend.service.impl;

import com.vacation.Vacation_Planner_Backend.dto.team.request.CreateTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.request.JoinTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.response.*;
import com.vacation.Vacation_Planner_Backend.model.entity.Team;
import com.vacation.Vacation_Planner_Backend.model.entity.TeamMember;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.model.entity.VacationRequest;
import com.vacation.Vacation_Planner_Backend.model.enums.Status;
import com.vacation.Vacation_Planner_Backend.repository.TeamMemberRepository;
import com.vacation.Vacation_Planner_Backend.repository.TeamRepository;
import com.vacation.Vacation_Planner_Backend.repository.VacationRequestRepository;
import com.vacation.Vacation_Planner_Backend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final VacationRequestRepository vacationRequestRepository;

    @Override
    public CreateTeamResponse createTeam(CreateTeamRequest request, User currentUser) {
        // Generate unique invite code
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Team team = Team.builder()
                .name(request.getName())
                .employer(currentUser)
                .inviteCode(inviteCode)
                .inviteQrUrl("vacationplanner://join?code=" + inviteCode)
                .build();
        teamRepository.save(team);
        return new CreateTeamResponse(
                team.getId(),
                team.getName(),
                team.getInviteCode(),
                team.getInviteQrUrl(),
                team.getCreatedAt().toString()
        );
    }

    @Override
    public JoinTeamResponse joinTeam(JoinTeamRequest request, User currentUser) {
        // Find team by invite code
        Team team = teamRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        // Check if user already in team
        boolean alreadyMember = teamMemberRepository
                .existsByTeamAndUser(team, currentUser);
        if (alreadyMember) {
            throw new RuntimeException("Already a member of this team");
        }
        // Add user to team
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(currentUser)
                .build();
        teamMemberRepository.save(member);
        return new JoinTeamResponse(
                team.getId(),
                team.getName(),
                team.getEmployer().getName(),
                member.getJoinedAt().toString()
        );
    }

    @Override
    public List<TeamMemberResponse> getTeamMembers(User currentUser) {
        // Find team where user is employer OR member
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseGet(() -> {
                    // If not employer — find team via membership
                    return teamMemberRepository.findByUser(currentUser)
                            .stream()
                            .findFirst()
                            .map(TeamMember::getTeam)
                            .orElseThrow(() -> new RuntimeException("Team not found"));
                });

        return teamMemberRepository.findByTeam(team)
                .stream()
                .map(member -> new TeamMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        member.getUser().getEmail(),
                        member.getUser().getRole().name(),
                        member.getJoinedAt().toString()
                ))
                .toList();
    }

    @Override
    public List<TeamCalendarResponse> getTeamCalendar(User currentUser) {
        // Find team where user is employer OR member
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseGet(() -> teamMemberRepository.findByUser(currentUser)
                        .stream()
                        .findFirst()
                        .map(TeamMember::getTeam)
                        .orElseThrow(() -> new RuntimeException("Team not found")));
        List<TeamMember> members = teamMemberRepository.findByTeam(team);

        return members.stream()
                .map(member -> {
                    List<VacationPeriod> vacations = vacationRequestRepository
                            .findByUserAndStatus(member.getUser(), Status.APPROVED)
                            .stream()
                            .map(v -> new VacationPeriod(
                                    v.getStartDate(),
                                    v.getEndDate(),
                                    calculateDays(v),
                                    v.getStatus().name()
                            ))
                            .toList();

                    return new TeamCalendarResponse(
                            member.getUser().getId(),
                            member.getUser().getName(),
                            vacations
                    );
                })
                .toList();
    }

    // Calculate number of vacation days
    private int calculateDays(VacationRequest vacation) {
        return (int) (vacation.getEndDate().toEpochDay()
                - vacation.getStartDate().toEpochDay() + 1);
    }
}