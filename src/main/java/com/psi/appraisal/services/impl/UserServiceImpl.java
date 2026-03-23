package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.CreateUserRequest;
import com.psi.appraisal.dtos.UpdateUserRequest;
import com.psi.appraisal.dtos.UserResponse;
import com.psi.appraisal.entity.Department;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.exception.DuplicateResourceException;
import com.psi.appraisal.exception.ResourceNotFoundException;
import com.psi.appraisal.repository.DepartmentRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .jobTitle(request.getJobTitle())
                .isActive(true)
                .build();

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            user.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", request.getManagerId()));
            user.setManager(manager);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public UserResponse getMe(Long userId) {
        return mapToResponse(findById(userId));
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return mapToResponse(findById(userId));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithDetails()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getTeamByManager(Long managerId) {
        return userRepository.findByManagerId(managerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = findById(userId);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle());
        if (request.getIsActive() != null) user.setActive(request.getIsActive());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            user.setDepartment(dept);
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", request.getManagerId()));
            user.setManager(manager);
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setJobTitle(user.getJobTitle());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());

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
