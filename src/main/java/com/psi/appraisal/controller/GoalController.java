package com.psi.appraisal.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.psi.appraisal.dtos.ApiResponse;
import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;
import com.psi.appraisal.services.GoalService;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    // Manager: create a goal for an employee's appraisal
    // POST /api/goals?managerId=1
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @Valid @RequestBody CreateGoalRequest request,
            @RequestParam Long managerId) {

        GoalResponse response = goalService.createGoal(request, managerId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created", response));
    }

    // Any: get all goals for an appraisal
    // GET /api/goals/appraisal/{appraisalId}
    @GetMapping("/appraisal/{appraisalId}")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByAppraisal(
            @PathVariable Long appraisalId) {

        List<GoalResponse> responses = goalService.getGoalsByAppraisal(appraisalId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Employee: update progress on their own goal
    // PATCH /api/goals/{id}/progress?employeeId=1
    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody GoalProgressRequest request,
            @RequestParam Long employeeId) {

        GoalResponse response = goalService.updateProgress(id, request, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Goal progress updated", response));
    }

    // Manager: delete a goal
    // DELETE /api/goals/{id}?requesterId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id,
            @RequestParam Long requesterId) {

        goalService.deleteGoal(id, requesterId);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted", null));
    }
}