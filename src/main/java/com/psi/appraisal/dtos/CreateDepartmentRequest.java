package com.appraisal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
}
