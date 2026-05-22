package com.vacation.Vacation_Planner_Backend.service.impl;

import com.vacation.Vacation_Planner_Backend.dto.team.request.CreateTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.request.JoinTeamRequest;
import com.vacation.Vacation_Planner_Backend.dto.team.response.*;
import com.vacation.Vacation_Planner_Backend.model.entity.*;
import com.vacation.Vacation_Planner_Backend.model.enums.Status;
import com.vacation.Vacation_Planner_Backend.repository.TeamMemberRepository;
import com.vacation.Vacation_Planner_Backend.repository.TeamRepository;
import com.vacation.Vacation_Planner_Backend.repository.VacationBalanceRepository;
import com.vacation.Vacation_Planner_Backend.repository.VacationRequestRepository;
import com.vacation.Vacation_Planner_Backend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final VacationBalanceRepository vacationBalanceRepository;


    @Override
    public CreateTeamResponse createTeam(CreateTeamRequest request, User currentUser) {
        // Check if user already owns a team
        if (teamRepository.findByEmployer(currentUser).isPresent()) {
            throw new RuntimeException("You already have a team");
        }

        // Check if user is already a member of another team
        if (!teamMemberRepository.findByUser(currentUser).isEmpty()) {
            throw new RuntimeException("You are already a member of a team");
        }

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
        // Check if user already owns a team
        if (teamRepository.findByEmployer(currentUser).isPresent()) {
            throw new RuntimeException("You already have a team");
        }

        // Check if user is already a member of any team
        if (!teamMemberRepository.findByUser(currentUser).isEmpty()) {
            throw new RuntimeException("You are already a member of a team");
        }

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
        // Auto-create vacation balance for new member
        int currentYear = LocalDate.now().getYear();
        VacationBalance balance = VacationBalance.builder()
                .user(currentUser)
                .year(currentYear)
                .totalDays(28)
                .usedDays(0)
                .build();
        vacationBalanceRepository.save(balance);
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

        List<TeamCalendarResponse> result = new ArrayList<>();

        List<VacationPeriod> employerVacations = vacationRequestRepository
                .findByUserAndStatus(team.getEmployer(), Status.APPROVED)
                .stream()
                .map(v -> new VacationPeriod(v.getStartDate(), v.getEndDate(), calculateDays(v), v.getStatus().name()))
                .toList();
        result.add(new TeamCalendarResponse(team.getEmployer().getId(), team.getEmployer().getName(), employerVacations));

        teamMemberRepository.findByTeam(team).forEach(member -> {
            List<VacationPeriod> vacations = vacationRequestRepository
                    .findByUserAndStatus(member.getUser(), Status.APPROVED)
                    .stream()
                    .map(v -> new VacationPeriod(v.getStartDate(), v.getEndDate(), calculateDays(v), v.getStatus().name()))
                    .toList();
            result.add(new TeamCalendarResponse(member.getUser().getId(), member.getUser().getName(), vacations));
        });

        return result;
    }

    // Calculate number of vacation days
    private int calculateDays(VacationRequest vacation) {
        return (int) (vacation.getEndDate().toEpochDay()
                - vacation.getStartDate().toEpochDay() + 1);
    }


    @Override
    public TeamInfoResponse getTeamInfo(User currentUser) {
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return new TeamInfoResponse(
                team.getId(),
                team.getName(),
                team.getInviteCode(),
                team.getInviteQrUrl()
        );
    }
}