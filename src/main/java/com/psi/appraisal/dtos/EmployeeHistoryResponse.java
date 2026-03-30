package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.enums.AppraisalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class EmployeeHistoryResponse {
    private Long employeeId;
    private String employeeName;
    private List<CycleRecord> cycles;

    @Getter
    @Builder
    public static class CycleRecord {
        private String cycleName;
        private LocalDate cycleStartDate;
        private LocalDate cycleEndDate;
        private Integer selfRating;
        private Integer managerRating;
        private AppraisalStatus status;
        private String managerName;
    }
}
