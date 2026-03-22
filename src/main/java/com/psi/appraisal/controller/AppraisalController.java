package com.psi.appraisal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.psi.appraisal.dtos.ApiResponse;
import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;
import com.psi.appraisal.services.AppraisalService;

import java.util.List;
 
@RestController
@RequestMapping("/api/appraisals")
@RequiredArgsConstructor
public class AppraisalController {
 
    private final AppraisalService appraisalService;
 
    // HR: create appraisal for an employee in a cycle
    // POST /api/appraisals
    @PostMapping
    public ResponseEntity<ApiResponse<AppraisalResponse>> createAppraisal(
            @Valid @RequestBody CreateAppraisalRequest request) {
 
        AppraisalResponse response = appraisalService.createAppraisal(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appraisal created successfully", response));
    }
 
    // Employee: get all my appraisals
    // GET /api/appraisals/my?employeeId=1
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getMyAppraisals(
            @RequestParam Long employeeId) {
 
        List<AppraisalResponse> responses = appraisalService.getMyAppraisals(employeeId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
 
    // Any role: get one appraisal by ID
    // GET /api/appraisals/{id}?requesterId=1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppraisalResponse>> getAppraisalById(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
 
        AppraisalResponse response = appraisalService.getAppraisalById(id, requesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
 
    // Employee: submit self-assessment
    // PUT /api/appraisals/{id}/self-assessment?employeeId=1
    @PutMapping("/{id}/self-assessment")
    public ResponseEntity<ApiResponse<AppraisalResponse>> submitSelfAssessment(
            @PathVariable Long id,
            @Valid @RequestBody SelfAssessmentRequest request,
            @RequestParam Long employeeId) {
 
        AppraisalResponse response = appraisalService.submitSelfAssessment(id, request, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Self-assessment submitted", response));
    }
 
    // Manager: submit review and rating
    // PUT /api/appraisals/{id}/manager-review?managerId=1
    @PutMapping("/{id}/manager-review")
    public ResponseEntity<ApiResponse<AppraisalResponse>> submitManagerReview(
            @PathVariable Long id,
            @Valid @RequestBody ManagerReviewRequest request,
            @RequestParam Long managerId) {
 
        AppraisalResponse response = appraisalService.submitManagerReview(id, request, managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager review submitted", response));
    }
 
    // HR: approve the final appraisal
    // PATCH /api/appraisals/{id}/approve
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AppraisalResponse>> approveAppraisal(
            @PathVariable Long id) {
 
        AppraisalResponse response = appraisalService.approveAppraisal(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal approved", response));
    }
 
    // Employee: acknowledge the final result
    // PATCH /api/appraisals/{id}/acknowledge?employeeId=1
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AppraisalResponse>> acknowledgeAppraisal(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
 
        AppraisalResponse response = appraisalService.acknowledgeAppraisal(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal acknowledged", response));
    }
}