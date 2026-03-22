package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.UserResponse;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserResponse getMe(Long userId) {
        User user = findById(userId);
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = findById(userId);
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getTeamByManager(Long managerId) {
        return userRepository.findByManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);

        // Nested fields ModelMapper can't auto-resolve
        if (user.getDepartment() != null) {
            response.setDepartmentName(user.getDepartment().getName());
        }
        if (user.getManager() != null) {
            response.setManagerId(user.getManager().getId());
            response.setManagerName(user.getManager().getFullName());
        }
        return response;
    }
}
