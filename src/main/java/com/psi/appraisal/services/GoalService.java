package com.psi.appraisal.services;



import java.util.List;

import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;
import com.psi.appraisal.dtos.UpdateGoalRequest;

public interface GoalService {

    GoalResponse createGoal(CreateGoalRequest request, Long managerId);

    GoalResponse getGoalById(Long goalId);

    List<GoalResponse> getGoalsByAppraisal(Long appraisalId);

    List<GoalResponse> getGoalsByEmployee(Long employeeId);

    GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long managerId);

    GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId);

    void deleteGoal(Long goalId, Long managerId);
}

