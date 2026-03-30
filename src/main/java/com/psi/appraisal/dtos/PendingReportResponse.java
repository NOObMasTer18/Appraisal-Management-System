package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.enums.AppraisalStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PendingReportResponse {
    private String cycleName;
    private long totalPending;
    private List<PendingEntry> entries;

    @Getter
    @Builder
    public static class PendingEntry {
        private Long employeeId;
        private String employeeName;
        private String managerName;
        private String departmentName;
        private AppraisalStatus currentStatus;
    }
}
