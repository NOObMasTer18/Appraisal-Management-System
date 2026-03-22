package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Feedback.FeedbackType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {

    @NotNull(message = "Appraisal ID is required")
    private Long appraisalId;

    @NotNull(message = "Reviewee ID is required")
    private Long revieweeId;

    @NotBlank(message = "Comments cannot be empty")
    private String comments;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotNull(message = "Feedback type is required")
    private FeedbackType feedbackType;
}
