package com.psi.appraisal.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfAssessmentRequest {

    @NotBlank(message = "Self assessment cannot be empty")
    private String selfAssessment;

    @NotNull(message = "Self rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer selfRating;
}
