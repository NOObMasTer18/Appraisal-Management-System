package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Notification.Type;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.entity.enums.AppraisalStatus;
import com.psi.appraisal.entity.enums.CycleStatus;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.AppraisalService;
import com.psi.appraisal.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppraisalServiceImpl implements AppraisalService {

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AppraisalResponse createAppraisal(CreateAppraisalRequest request) {

        // Guard: no duplicate appraisal for same employee in same cycle
        if (appraisalRepository.existsByCycleNameAndEmployeeId(
                request.getCycleName(), request.getEmployeeId())) {
            throw new RuntimeException("Appraisal already exists for this employee in cycle: "
                    + request.getCycleName());
        }

        User employee = findUserById(request.getEmployeeId());
        User manager  = findUserById(request.getManagerId());

        Appraisal appraisal = Appraisal.builder()
                .cycleName(request.getCycleName())
                .cycleStartDate(request.getCycleStartDate())
                .cycleEndDate(request.getCycleEndDate())
                .cycleStatus(CycleStatus.ACTIVE)
                .employee(employee)
                .manager(manager)
                .appraisalStatus(AppraisalStatus.PENDING)
                .build();

        appraisalRepository.save(appraisal);

        // Notify employee that their appraisal cycle has started
        notificationService.send(
                employee.getId(),
                "Appraisal cycle started",
                "Your appraisal for cycle '" + request.getCycleName() + "' has been created. Please submit your self-assessment.",
                Type.CYCLE_STARTED
        );

        return mapToResponse(appraisal);
    }

    @Override
    public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
        return appraisalRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        // Ownership check: only the employee, their manager, or HR can view
        boolean isEmployee = appraisal.getEmployee().getId().equals(requesterId);
        boolean isManager  = appraisal.getManager().getId().equals(requesterId);

        if (!isEmployee && !isManager) {
            throw new RuntimeException("Access denied: you are not part of this appraisal");
        }

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse submitSelfAssessment(Long appraisalId,
                                                   SelfAssessmentRequest request,
                                                   Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        // Ownership check: must be the appraisal's own employee
        if (!appraisal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Access denied: this is not your appraisal");
        }

        // Status check: can only submit if currently PENDING
        if (appraisal.getAppraisalStatus() != AppraisalStatus.PENDING) {
            throw new RuntimeException("Self-assessment already submitted. Current status: "
                    + appraisal.getAppraisalStatus());
        }

        appraisal.setSelfAssessment(request.getSelfAssessment());
        appraisal.setSelfRating(request.getSelfRating());
        appraisal.setAppraisalStatus(AppraisalStatus.SELF_SUBMITTED);
        appraisal.setSubmittedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        // Notify manager they have a pending review
        notificationService.send(
                appraisal.getManager().getId(),
                "Self-assessment submitted",
                appraisal.getEmployee().getFullName() + " has submitted their self-assessment for '"
                        + appraisal.getCycleName() + "'. Please review and rate.",
                Type.SELF_ASSESSMENT_SUBMITTED
        );

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse submitManagerReview(Long appraisalId,
                                                  ManagerReviewRequest request,
                                                  Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        // Ownership check: must be the assigned manager
        if (!appraisal.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Access denied: you are not the manager for this appraisal");
        }

        // Status check: employee must have submitted first
        if (appraisal.getAppraisalStatus() != AppraisalStatus.SELF_SUBMITTED) {
            throw new RuntimeException("Cannot review yet. Current status: "
                    + appraisal.getAppraisalStatus());
        }

        appraisal.setManagerComments(request.getManagerComments());
        appraisal.setManagerRating(request.getManagerRating());
        appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_REVIEWED);
        appraisalRepository.save(appraisal);

        // Notify HR that this appraisal is ready for approval
        notificationService.send(
                appraisal.getEmployee().getId(),
                "Manager review completed",
                "Your manager has reviewed your appraisal for '"
                        + appraisal.getCycleName() + "'. Awaiting HR approval.",
                Type.MANAGER_REVIEW_DONE
        );

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse approveAppraisal(Long appraisalId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        // Status check: manager must have reviewed first
        if (appraisal.getAppraisalStatus() != AppraisalStatus.MANAGER_REVIEWED) {
            throw new RuntimeException("Cannot approve yet. Current status: "
                    + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.APPROVED);
        appraisal.setApprovedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        // Notify employee their result is ready
        notificationService.send(
                appraisal.getEmployee().getId(),
                "Appraisal approved",
                "Your appraisal for '" + appraisal.getCycleName()
                        + "' has been approved. Please review and acknowledge.",
                Type.APPRAISAL_APPROVED
        );

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        // Ownership check
        if (!appraisal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Access denied: this is not your appraisal");
        }

        // Status check: must be approved before acknowledgement
        if (appraisal.getAppraisalStatus() != AppraisalStatus.APPROVED) {
            throw new RuntimeException("Cannot acknowledge yet. Current status: "
                    + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.ACKNOWLEDGED);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Appraisal findAppraisalById(Long id) {
        return appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found with id: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // ModelMapper maps most fields automatically.
    // Employee/manager names need manual mapping since they are
    // nested objects (appraisal.employee.fullName → employeeName)
    private AppraisalResponse mapToResponse(Appraisal appraisal) {
        AppraisalResponse response = modelMapper.map(appraisal, AppraisalResponse.class);
        response.setEmployeeId(appraisal.getEmployee().getId());
        response.setEmployeeName(appraisal.getEmployee().getFullName());
        response.setManagerId(appraisal.getManager().getId());
        response.setManagerName(appraisal.getManager().getFullName());
        return response;
    }
}
