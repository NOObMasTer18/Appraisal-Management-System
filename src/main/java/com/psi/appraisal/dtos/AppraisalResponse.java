package com.psi.appraisal.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.psi.appraisal.entity.enums.AppraisalStatus;
import com.psi.appraisal.entity.enums.CycleStatus;

@Getter
@Setter
public class AppraisalResponse {

    private Long id;

    // Cycle info
    private String cycleName;
    private LocalDate cycleStartDate;
    private LocalDate cycleEndDate;
    private CycleStatus cycleStatus;

    // Participants (just names + IDs — never expose password)
    private Long employeeId;
    private String employeeName;
    private Long managerId;
    private String managerName;

    // Self assessment
    private String selfAssessment;
    private Integer selfRating;

    // Manager review
    private String managerComments;
    private Integer managerRating;

    // Status & timestamps
    private AppraisalStatus appraisalStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
