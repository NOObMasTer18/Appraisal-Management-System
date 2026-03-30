package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Goal.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoalResponse {

    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private Status status;
    private LocalDate dueDate;
}
