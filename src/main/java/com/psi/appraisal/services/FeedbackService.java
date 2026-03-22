package com.psi.appraisal.services;



import java.util.List;

import com.psi.appraisal.dtos.FeedbackRequest;
import com.psi.appraisal.dtos.FeedbackResponse;

public interface FeedbackService {

    // Employee/Manager: submit peer feedback
    FeedbackResponse submitFeedback(FeedbackRequest request, Long reviewerId);

    // Manager/HR: get all feedback for an appraisal
    List<FeedbackResponse> getFeedbackByAppraisal(Long appraisalId);

    // Employee: get all feedback received
    List<FeedbackResponse> getFeedbackForEmployee(Long employeeId);
}
