package com.psi.appraisal.services;



import java.util.List;

import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;

public interface GoalService {

    // Manager/HR: create a goal linked to an appraisal
    GoalResponse createGoal(CreateGoalRequest request, Long managerId);

    // Any: get all goals for an appraisal
    List<GoalResponse> getGoalsByAppraisal(Long appraisalId);

    // Employee: update their own goal progress
    GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId);

    // Manager/HR: delete a goal
    void deleteGoal(Long goalId, Long requesterId);
}
