package com.vacation.Vacation_Planner_Backend.service;

import com.vacation.Vacation_Planner_Backend.dto.user.request.UpdateProfileRequest;
import com.vacation.Vacation_Planner_Backend.dto.user.response.UserProfileResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;

public interface UserService {
    public UserProfileResponse getCurrentUserProfile(User currentUser);
    public UserProfileResponse updateUser(User currentUser, UpdateProfileRequest request);
}
