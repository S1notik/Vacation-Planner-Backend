package com.vacation.Vacation_Planner_Backend.service;

import com.vacation.Vacation_Planner_Backend.dto.vacation.request.CreateVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.request.ReviewVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationBalanceResponse;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface VacationService {
    VacationResponse createVacation(CreateVacationRequest request, User currentUser);
    List<VacationResponse> viewVacationHistory(User currenUser);
    VacationBalanceResponse viewTotalVacationBalance(User currentUser);
    List<VacationResponse> viewAllRequestToVacation(User currentUser);
    VacationResponse reviewVacation(UUID vacationId, ReviewVacationRequest request, User currentUser);
    VacationResponse cancelVacation(UUID vacationId, User currentUser);
    VacationBalanceResponse setEmployeeBalance(UUID employeeId, int totalDays, User currentUser);
    List<VacationBalanceResponse> setTeamBalance(int totalDays, User currentUser);
}
