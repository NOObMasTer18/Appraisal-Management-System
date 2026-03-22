package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Goal.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalProgressRequest {

    @NotNull(message = "Progress percent is required")
    @Min(value = 0, message = "Progress cannot be less than 0")
    @Max(value = 100, message = "Progress cannot exceed 100")
    private Integer progressPercent;

    @NotNull(message = "Status is required")
    private Status status;
}
