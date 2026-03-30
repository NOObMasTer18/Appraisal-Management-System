package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Goal.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalProgressRequest {

    @NotNull(message = "Status is required")
    private Status status;
}
