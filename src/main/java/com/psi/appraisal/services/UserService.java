package com.psi.appraisal.services;

import java.util.List;

import com.psi.appraisal.dtos.CreateUserRequest;
import com.psi.appraisal.dtos.UpdateUserRequest;
import com.psi.appraisal.dtos.UserResponse;
import jakarta.validation.Valid;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getMe(Long userId);

    UserResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();

    List<UserResponse> getTeamByManager(Long managerId);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    void deleteUser(Long userId);
}

