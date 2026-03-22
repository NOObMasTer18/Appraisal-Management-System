package com.psi.appraisal.services;

import java.util.List;

import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;

public interface AppraisalService {

    // HR: create a new appraisal for an employee in a cycle
    AppraisalResponse createAppraisal(CreateAppraisalRequest request);

    // Employee: view all their own appraisals
    List<AppraisalResponse> getMyAppraisals(Long employeeId);

    // Any role: view one appraisal by ID (with ownership check)
    AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId);

    // Employee: submit self-assessment — moves status to SELF_SUBMITTED
    AppraisalResponse submitSelfAssessment(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

    // Manager: submit review — moves status to MANAGER_REVIEWED
    AppraisalResponse submitManagerReview(Long appraisalId, ManagerReviewRequest request, Long managerId);

    // HR: approve final appraisal — moves status to APPROVED
    AppraisalResponse approveAppraisal(Long appraisalId);

    // Employee: acknowledge result — moves status to ACKNOWLEDGED
    AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId);
}
