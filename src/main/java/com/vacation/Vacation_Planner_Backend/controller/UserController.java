package com.vacation.Vacation_Planner_Backend.controller;


import com.vacation.Vacation_Planner_Backend.dto.user.request.UpdateProfileRequest;
import com.vacation.Vacation_Planner_Backend.dto.user.response.UserProfileResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe() {
        return ResponseEntity.ok(userService.getCurrentUserProfile(getCurrentUser()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @RequestBody UpdateProfileRequest request
            ) {
        return ResponseEntity.ok(userService.updateUser(getCurrentUser(), request));
    }
}
