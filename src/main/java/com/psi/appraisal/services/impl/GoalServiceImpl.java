package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;
import com.psi.appraisal.dtos.UpdateGoalRequest;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Goal;
import com.psi.appraisal.exception.ResourceNotFoundException;
import com.psi.appraisal.exception.UnauthorizedAccessException;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.GoalRepository;
import com.psi.appraisal.services.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final AppraisalRepository appraisalRepository;

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request, Long managerId) {
        Appraisal appraisal = appraisalRepository.findByIdWithDetails(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal", request.getAppraisalId()));

        if (!appraisal.getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException(
                    "Access denied: you are not the manager for this appraisal");
        }

        Goal goal = Goal.builder()
                .appraisal(appraisal)
                .employee(appraisal.getEmployee())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .build();

        goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Override
    public GoalResponse getGoalById(Long goalId) {
        return mapToResponse(findById(goalId));
    }

    @Override
    public List<GoalResponse> getGoalsByAppraisal(Long appraisalId) {
        return goalRepository.findByAppraisalId(appraisalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalResponse> getGoalsByEmployee(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long managerId) {
        Goal goal = findById(goalId);

        if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException(
                    "Access denied: only the manager can update this goal");
        }

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getDueDate() != null) goal.setDueDate(request.getDueDate());

        goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
        Goal goal = findById(goalId);

        if (!goal.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedAccessException("Access denied: this is not your goal");
        }

        goal.setStatus(request.getStatus());
        goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId, Long managerId) {
        Goal goal = findById(goalId);

        if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException(
                    "Access denied: only the manager can delete this goal");
        }

        goalRepository.delete(goal);
    }

    private Goal findById(Long id) {
        return goalRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
    }

    private GoalResponse mapToResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setAppraisalId(goal.getAppraisal().getId());
        response.setEmployeeId(goal.getEmployee().getId());
        response.setEmployeeName(goal.getEmployee().getFullName());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setStatus(goal.getStatus());
        response.setDueDate(goal.getDueDate());
        return response;
    }
}
