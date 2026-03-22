package com.psi.appraisal.services;

import java.util.List;

import com.psi.appraisal.dtos.UserResponse;

public interface UserService {

    UserResponse getMe(Long userId);

    UserResponse getUserById(Long userId);

    List<UserResponse> getTeamByManager(Long managerId);
}
