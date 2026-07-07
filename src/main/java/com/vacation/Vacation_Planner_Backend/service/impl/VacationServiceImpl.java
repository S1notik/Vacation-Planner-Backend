package com.vacation.Vacation_Planner_Backend.service.impl;

import com.vacation.Vacation_Planner_Backend.dto.vacation.request.CreateVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.request.ReviewVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationBalanceResponse;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationResponse;
import com.vacation.Vacation_Planner_Backend.exception.BadRequestException;
import com.vacation.Vacation_Planner_Backend.exception.NotFoundException;
import com.vacation.Vacation_Planner_Backend.model.entity.*;
import com.vacation.Vacation_Planner_Backend.model.enums.NotificationType;
import com.vacation.Vacation_Planner_Backend.model.enums.Status;
import com.vacation.Vacation_Planner_Backend.repository.*;
import com.vacation.Vacation_Planner_Backend.service.VacationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.vacation.Vacation_Planner_Backend.model.entity.Notification;
import com.vacation.Vacation_Planner_Backend.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacationServiceImpl implements VacationService {

    private final VacationBalanceRepository vacationBalanceRepository;
    private final VacationRequestRepository vacationRequestRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final NotificationRepository notificationRepository;


    @Override
    public VacationResponse createVacation(CreateVacationRequest request, User currentUser) {
        // Find user's team
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseGet(() -> teamMemberRepository.findByUser(currentUser)
                        .stream()
                        .findFirst()
                        .map(TeamMember::getTeam)
                        .orElseThrow(() -> new BadRequestException("User is not in a team")));
        int currentYear = LocalDate.now().getYear();
        int usedDays = calculateUsedDays(currentUser, currentYear);
        VacationBalance balance = vacationBalanceRepository
                .findByUserAndYear(currentUser, currentYear)
                .orElseGet(() -> {
                    VacationBalance newBalance = VacationBalance.builder()
                            .user(currentUser)
                            .year(currentYear)
                            .totalDays(28)
                            .usedDays(0)
                            .build();
                    return vacationBalanceRepository.save(newBalance);
                });
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        long daysCount = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (balance.getTotalDays() - usedDays < daysCount) {
            throw new BadRequestException("Not enough vacation days");
        }
        boolean isEmployer = team.getEmployer().getId().equals(currentUser.getId());
        Status status = isEmployer ? Status.APPROVED : Status.PENDING;
        VacationRequest vacation = VacationRequest.builder()
                .user(currentUser)
                .team(team)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .comment(request.getComment())
                .build();

        vacationRequestRepository.save(vacation);

        // Notify employer about new vacation request
        if (!isEmployer) {
            Notification notification = Notification.builder()
                    .user(team.getEmployer())
                    .type(NotificationType.VACATION_SUBMITTED)
                    .message("Сотрудник " + currentUser.getName() + " подал заявку на отпуск с "
                            + request.getStartDate() + " по " + request.getEndDate())
                    .build();
            notificationRepository.save(notification);
        }
        return new VacationResponse(
                vacation.getId(),
                vacation.getStartDate(),
                vacation.getEndDate(),
                (int) vacation.durationDays(),
                vacation.getStatus().name(),
                vacation.getComment(),
                vacation.getCreatedAt().toString()
        );
    }

    @Override
    public List<VacationResponse> viewVacationHistory(User currentUser) {
        return vacationRequestRepository.findByUser(currentUser)
                .stream()
                .map(v -> new VacationResponse(
                        v.getId(),
                        v.getStartDate(),
                        v.getEndDate(),
                        (int) v.durationDays(),
                        v.getStatus().name(),
                        v.getComment(),
                        v.getCreatedAt().toString()
                ))
                .toList();
    }

    @Override
    public VacationBalanceResponse viewTotalVacationBalance(User currentUser) {
        int currentYear = LocalDate.now().getYear();
        VacationBalance balance = vacationBalanceRepository
                .findByUserAndYear(currentUser, currentYear)
                .orElseThrow(() -> new NotFoundException("Vacation balance not found"));

        int usedDays = calculateUsedDays(currentUser, currentYear);
        return new VacationBalanceResponse(
                balance.getTotalDays(),
                usedDays,
                balance.getTotalDays() - usedDays,
                currentYear
        );
    }

    @Override
    public List<VacationResponse> viewAllRequestToVacation(User currentUser) {
        // Find team where user is employer
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseThrow(() -> new NotFoundException("Team not found"));

        return vacationRequestRepository.findByTeam(team)
                .stream()
                .map(v -> new VacationResponse(
                        v.getId(),
                        v.getStartDate(),
                        v.getEndDate(),
                        (int) v.durationDays(),
                        v.getStatus().name(),
                        v.getComment(),
                        v.getCreatedAt().toString()
                ))
                .toList();
    }

    @Override
    public VacationResponse reviewVacation(UUID vacationId, ReviewVacationRequest request, User currentUser) {
        // Find vacation request
        VacationRequest vacation = vacationRequestRepository.findById(vacationId)
                .orElseThrow(() -> new NotFoundException("Vacation request not found"));

        if (vacation.getTeam() == null
                || vacation.getTeam().getEmployer() == null
                || !vacation.getTeam().getEmployer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only review vacations from your own team");
        }

        if (vacation.getStatus() != Status.PENDING) {
            throw new BadRequestException("Vacation request is already reviewed");
        }

        // Update status
        Status newStatus;
        try {
            newStatus = Status.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status");
        }
        vacation.setStatus(newStatus);
        vacation.setReviewedBy(currentUser);
        vacation.setReviewedAt(java.time.LocalDateTime.now());
        vacationRequestRepository.save(vacation);

        // Notify employee about review decision
        NotificationType notifType = newStatus == Status.APPROVED
                ? NotificationType.VACATION_APPROVED
                : NotificationType.VACATION_REJECTED;

        String notifMessage = newStatus == Status.APPROVED
                ? "Ваша заявка на отпуск с " + vacation.getStartDate() + " по " + vacation.getEndDate() + " одобрена"
                : "Ваша заявка на отпуск с " + vacation.getStartDate() + " по " + vacation.getEndDate() + " отклонена";

        Notification notification = Notification.builder()
                .user(vacation.getUser())
                .type(notifType)
                .message(notifMessage)
                .build();
        notificationRepository.save(notification);

        return new VacationResponse(
                vacation.getId(),
                vacation.getStartDate(),
                vacation.getEndDate(),
                (int) vacation.durationDays(),
                vacation.getStatus().name(),
                vacation.getComment(),
                vacation.getCreatedAt().toString()
        );
    }

    @Override
    public VacationResponse cancelVacation(UUID vacationId, User currentUser) {
        // Find vacation request
        VacationRequest vacation = vacationRequestRepository.findById(vacationId)
                .orElseThrow(() -> new BadRequestException("Vacation request not found"));
        // Only owner can cancel
        if (!vacation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only cancel your own vacation requests");
        }
        // Only PENDING can be cancelled
        if (!vacation.isPending()) {
            throw new BadRequestException("Only pending vacation requests can be cancelled");
        }
        vacation.setStatus(Status.CANCELLED);
        vacationRequestRepository.save(vacation);
        return new VacationResponse(
                vacation.getId(),
                vacation.getStartDate(),
                vacation.getEndDate(),
                (int) vacation.durationDays(),
                vacation.getStatus().name(),
                vacation.getComment(),
                vacation.getCreatedAt().toString()
        );
    }

    @Override
    public VacationBalanceResponse setEmployeeBalance(UUID employeeId, int totalDays, User currentUser) {
        // Find employee
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        Team team = teamRepository.findByEmployer(currentUser)
                .orElseThrow(() -> new NotFoundException("Team not found"));
        boolean isOwnTeamMember = teamMemberRepository.existsByTeamAndUser(team, employee);
        boolean isSelf = employee.getId().equals(currentUser.getId());
        if (!isOwnTeamMember && !isSelf) {
            throw new AccessDeniedException("You can only set balance for members of your own team");
        }

        int currentYear = LocalDate.now().getYear();

        // Find or create balance
        VacationBalance balance = vacationBalanceRepository
                .findByUserAndYear(employee, currentYear)
                .orElse(VacationBalance.builder()
                        .user(employee)
                        .year(currentYear)
                        .usedDays(0)
                        .build());

        balance.setTotalDays(totalDays);
        vacationBalanceRepository.save(balance);

        return new VacationBalanceResponse(
                balance.getTotalDays(),
                balance.getUsedDays(),
                balance.remainingDays(),
                currentYear
        );
    }

    @Override
    public List<VacationBalanceResponse> setTeamBalance(int totalDays, User currentUser) {
        // Find employer's team
        Team team = teamRepository.findByEmployer(currentUser)
                .orElseThrow(() -> new NotFoundException("Team not found"));

        int currentYear = LocalDate.now().getYear();

        List<User> users = new java.util.ArrayList<>(
                teamMemberRepository.findByTeam(team).stream()
                        .map(TeamMember::getUser)
                        .toList()
        );

        users.add(team.getEmployer());
        // Update balance for all team members
        return users.stream()
                .map(user -> {
                    VacationBalance balance = vacationBalanceRepository
                            .findByUserAndYear(user, currentYear)
                            .orElse(VacationBalance.builder()
                                    .user(user)
                                    .year(currentYear)
                                    .usedDays(0)
                                    .build());
                    balance.setTotalDays(totalDays);
                    vacationBalanceRepository.save(balance);
                    return new VacationBalanceResponse(
                            balance.getTotalDays(),
                            balance.getUsedDays(),
                            balance.remainingDays(),
                            currentYear
                    );
                })
                .toList();
    }

    private int calculateUsedDays(User user, int year) {
        return vacationRequestRepository
                .findByUserAndStatus(user, Status.APPROVED)
                .stream()
                .filter(v -> v.getStartDate().getYear() == year)
                .mapToInt(v -> (int) v.durationDays())
                .sum();
    }

}
