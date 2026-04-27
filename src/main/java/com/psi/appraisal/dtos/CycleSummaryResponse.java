package com.psi.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CycleSummaryResponse {
    private String cycleName;
    private long totalAppraisals;
    private long pendingCount;
    private long completedCount;
    private double completionPercentage;
    private Double averageRating;

    // Detailed breakdown (optional for frontend but kept for internal use if needed)
    private long pending;
    private long employeeDraft;
    private long selfSubmitted;
    private long managerDraft;
    private long managerReviewed;
    private long approved;
    private long acknowledged;
}
