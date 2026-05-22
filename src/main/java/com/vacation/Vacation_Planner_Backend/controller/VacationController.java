package com.vacation.Vacation_Planner_Backend.controller;


import com.vacation.Vacation_Planner_Backend.dto.vacation.request.CreateVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.request.ReviewVacationRequest;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationBalanceResponse;
import com.vacation.Vacation_Planner_Backend.dto.vacation.response.VacationResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.service.VacationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
public class VacationController {

    private final VacationService vacationService;

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // Employee creates vacation request
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('EMPLOYER')")
    public ResponseEntity<VacationResponse> createVacation(
            @Valid @RequestBody CreateVacationRequest request) {
        return ResponseEntity.ok(vacationService.createVacation(request, getCurrentUser()));
    }

    // Employee views their vacation history
    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<VacationResponse>> viewVacationHistory() {
        return ResponseEntity.ok(vacationService.viewVacationHistory(getCurrentUser()));
    }

    // Employee views vacation balance
    @GetMapping("/balance")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<VacationBalanceResponse> viewVacationBalance() {
        return ResponseEntity.ok(vacationService.viewTotalVacationBalance(getCurrentUser()));
    }

    // Employer views all team vacation requests
    @GetMapping("/team")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<VacationResponse>> viewAllVacationRequests() {
        return ResponseEntity.ok(vacationService.viewAllRequestToVacation(getCurrentUser()));
    }

    // Employer approves or rejects vacation
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<VacationResponse> reviewVacation(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewVacationRequest request) {
        return ResponseEntity.ok(vacationService.reviewVacation(id, request, getCurrentUser()));
    }

    // Employee cancels vacation request
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<VacationResponse> cancelVacation(@PathVariable UUID id) {
        return ResponseEntity.ok(vacationService.cancelVacation(id, getCurrentUser()));
    }

    // Employer sets balance for specific employee
    @PutMapping("/balance/{employeeId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<VacationBalanceResponse> setEmployeeBalance(
            @PathVariable UUID employeeId,
            @RequestParam int totalDays) {
        return ResponseEntity.ok(vacationService.setEmployeeBalance(employeeId, totalDays, getCurrentUser()));
    }

    // Employer sets balance for all team members
    @PutMapping("/balance/team")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<VacationBalanceResponse>> setTeamBalance(
            @RequestParam int totalDays) {
        return ResponseEntity.ok(vacationService.setTeamBalance(totalDays, getCurrentUser()));
    }

}
