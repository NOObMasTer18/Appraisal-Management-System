package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Goal;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.GoalRepository;
import com.psi.appraisal.services.GoalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final AppraisalRepository appraisalRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request, Long managerId) {
        Appraisal appraisal = appraisalRepository.findById(request.getAppraisalId())
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        // Only the assigned manager can add goals to this appraisal
        if (!appraisal.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Access denied: you are not the manager for this appraisal");
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
    public List<GoalResponse> getGoalsByAppraisal(Long appraisalId) {
        return goalRepository.findByAppraisalId(appraisalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Ownership check: only the goal's employee can update progress
        if (!goal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Access denied: this is not your goal");
        }

        goal.setProgressPercent(request.getProgressPercent());
        goal.setStatus(request.getStatus());
        goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId, Long requesterId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Only the assigned manager can delete
        if (!goal.getAppraisal().getManager().getId().equals(requesterId)) {
            throw new RuntimeException("Access denied: only the manager can delete this goal");
        }

        goalRepository.delete(goal);
    }

    private GoalResponse mapToResponse(Goal goal) {
        GoalResponse response = modelMapper.map(goal, GoalResponse.class);
        response.setAppraisalId(goal.getAppraisal().getId());
        response.setEmployeeId(goal.getEmployee().getId());
        response.setEmployeeName(goal.getEmployee().getFullName());
        return response;
    }
}
