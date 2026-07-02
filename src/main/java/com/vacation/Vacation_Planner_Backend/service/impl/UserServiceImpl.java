package com.vacation.Vacation_Planner_Backend.service.impl;


import com.vacation.Vacation_Planner_Backend.dto.user.request.UpdateProfileRequest;
import com.vacation.Vacation_Planner_Backend.dto.user.response.UserProfileResponse;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.repository.UserRepository;
import com.vacation.Vacation_Planner_Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    public UserProfileResponse getCurrentUserProfile(User currentUser) {
        return new UserProfileResponse (
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole().name(),
                currentUser.getPhone(),
                currentUser.getJobTitle(),
                currentUser.getAvatarUrl()
        );
    }

    @Override
    public UserProfileResponse updateUser(User currentUser, UpdateProfileRequest request) {
        if (request.getName() != null) currentUser.setName(request.getName());
        if (request.getPhone() != null) currentUser.setPhone(request.getPhone());
        if (request.getJobTitle() != null) currentUser.setJobTitle(request.getJobTitle());
        if (request.getAvatarUrl() != null) currentUser.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(currentUser);
        return new UserProfileResponse (
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getName(),
                currentUser.getRole().name(),
                currentUser.getPhone(),
                currentUser.getJobTitle(),
                currentUser.getAvatarUrl()
        );
    }
}
