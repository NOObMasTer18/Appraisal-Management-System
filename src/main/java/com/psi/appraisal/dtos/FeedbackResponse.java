package com.psi.appraisal.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.psi.appraisal.entity.Feedback.FeedbackType;

@Getter
@Setter
public class FeedbackResponse {

    private Long id;
    private Long appraisalId;
    private Long reviewerId;
    private String reviewerName;
    private Long revieweeId;
    private String revieweeName;
    private String comments;
    private Integer rating;
    private FeedbackType feedbackType;
    private LocalDateTime createdAt;
}
