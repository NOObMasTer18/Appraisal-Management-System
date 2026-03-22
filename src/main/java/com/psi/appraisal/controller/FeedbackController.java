package com.psi.appraisal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.psi.appraisal.dtos.ApiResponse;
import com.psi.appraisal.dtos.FeedbackRequest;
import com.psi.appraisal.dtos.FeedbackResponse;
import com.psi.appraisal.services.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // Employee/Manager: submit peer or manager feedback
    // POST /api/feedback?reviewerId=1
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @RequestParam Long reviewerId) {

        FeedbackResponse response = feedbackService.submitFeedback(request, reviewerId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted", response));
    }

    // Manager/HR: get all feedback for an appraisal
    // GET /api/feedback/appraisal/{appraisalId}
    @GetMapping("/appraisal/{appraisalId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbackByAppraisal(
            @PathVariable Long appraisalId) {

        List<FeedbackResponse> responses = feedbackService.getFeedbackByAppraisal(appraisalId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Employee: get all feedback received
    // GET /api/feedback/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbackForEmployee(
            @PathVariable Long employeeId) {

        List<FeedbackResponse> responses = feedbackService.getFeedbackForEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
