package com.psi.appraisal.services;

import com.psi.appraisal.dtos.*;

import java.util.List;

public interface ReportService {

    CycleSummaryResponse getCycleSummary(String cycleName);

    List<DepartmentReportResponse> getDepartmentReport(String cycleName);

    RatingDistributionResponse getRatingDistribution(String cycleName);

    PendingReportResponse getPendingReport(String cycleName);

    TeamReportResponse getTeamReport(String cycleName, Long managerId);

    EmployeeHistoryResponse getEmployeeHistory(Long employeeId);
}
