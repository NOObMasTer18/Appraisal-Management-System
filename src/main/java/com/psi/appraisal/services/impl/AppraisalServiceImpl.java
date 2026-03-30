package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.BulkCycleRequest;
import com.psi.appraisal.dtos.BulkCycleResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Notification.Type;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.entity.enums.AppraisalStatus;
import com.psi.appraisal.entity.enums.CycleStatus;
import com.psi.appraisal.entity.enums.Role;
import com.psi.appraisal.exception.InvalidStatusTransitionException;
import com.psi.appraisal.exception.UnauthorizedAccessException;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.AppraisalService;
import com.psi.appraisal.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppraisalServiceImpl implements AppraisalService {

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ── Create ────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse createAppraisal(CreateAppraisalRequest request) {
        if (appraisalRepository.existsByCycleNameAndEmployeeId(
                request.getCycleName(), request.getEmployeeId())) {
            throw new RuntimeException("Appraisal already exists for this employee in cycle: "
                    + request.getCycleName());
        }

        User employee = findUserById(request.getEmployeeId());
        User manager  = findUserById(request.getManagerId());

        if (employee.getRole() != Role.EMPLOYEE)
            throw new RuntimeException("The assigned employee must have the EMPLOYEE role");
        if (manager.getRole() != Role.MANAGER)
            throw new RuntimeException("The assigned manager must have the MANAGER role");

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

        notificationService.send(
                employee.getId(),
                "Appraisal cycle started",
                "Your appraisal for cycle '" + request.getCycleName()
                        + "' has been created. Please submit your self-assessment.",
                Type.CYCLE_STARTED
        );

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public BulkCycleResponse createBulkCycle(BulkCycleRequest request) {
        List<User> employees = userRepository.findByRoleAndIsActiveTrue(Role.EMPLOYEE);

        int created = 0, skippedAlreadyExists = 0, skippedNoManager = 0;

        for (User employee : employees) {
            if (employee.getManager() == null) {
                log.warn("Skipping employee {} (id={}) — no manager assigned",
                        employee.getFullName(), employee.getId());
                skippedNoManager++;
                continue;
            }
            if (appraisalRepository.existsByCycleNameAndEmployeeId(
                    request.getCycleName(), employee.getId())) {
                log.info("Skipping employee {} — appraisal already exists for cycle '{}'",
                        employee.getFullName(), request.getCycleName());
                skippedAlreadyExists++;
                continue;
            }

            Appraisal appraisal = Appraisal.builder()
                    .cycleName(request.getCycleName())
                    .cycleStartDate(request.getCycleStartDate())
                    .cycleEndDate(request.getCycleEndDate())
                    .cycleStatus(CycleStatus.ACTIVE)
                    .employee(employee)
                    .manager(employee.getManager())
                    .appraisalStatus(AppraisalStatus.PENDING)
                    .build();

            appraisalRepository.save(appraisal);

            notificationService.send(
                    employee.getId(),
                    "Appraisal cycle started",
                    "Your appraisal for cycle '" + request.getCycleName()
                            + "' has been created. Please submit your self-assessment.",
                    Type.CYCLE_STARTED
            );
            created++;
        }

        log.info("Bulk cycle '{}' — created: {}, skippedAlreadyExists: {}, skippedNoManager: {}",
                request.getCycleName(), created, skippedAlreadyExists, skippedNoManager);

        return new BulkCycleResponse(request.getCycleName(), employees.size(),
                created, skippedAlreadyExists, skippedNoManager);
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
        return appraisalRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AppraisalResponse> getTeamAppraisals(Long managerId) {
        return appraisalRepository.findByManagerId(managerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        boolean isEmployee = appraisal.getEmployee().getId().equals(requesterId);
        boolean isManager  = appraisal.getManager().getId().equals(requesterId);
        if (!isEmployee && !isManager)
            throw new UnauthorizedAccessException("Access denied: you are not part of this appraisal");
        return mapToResponse(appraisal);
    }

    // ── Self-assessment draft ─────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse saveSelfAssessmentDraft(Long appraisalId,
                                                     SelfAssessmentRequest request,
                                                     Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.PENDING && status != AppraisalStatus.EMPLOYEE_DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot save draft. Self-assessment is already submitted. Current status: " + status);
        }

        applySelfAssessmentFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.EMPLOYEE_DRAFT);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Self-assessment submit ────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse submitSelfAssessment(Long appraisalId,
                                                  SelfAssessmentRequest request,
                                                  Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.PENDING && status != AppraisalStatus.EMPLOYEE_DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot submit self-assessment. Current status: " + status);
        }

        applySelfAssessmentFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.SELF_SUBMITTED);
        appraisal.setSubmittedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        notificationService.send(
                appraisal.getManager().getId(),
                "Self-assessment submitted",
                appraisal.getEmployee().getFullName() + " has submitted their self-assessment for '"
                        + appraisal.getCycleName() + "'. Please review and rate.",
                Type.SELF_ASSESSMENT_SUBMITTED
        );

        return mapToResponse(appraisal);
    }

    // ── Manager review draft ──────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse saveManagerReviewDraft(Long appraisalId,
                                                    ManagerReviewRequest request,
                                                    Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireManager(appraisal, managerId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.SELF_SUBMITTED && status != AppraisalStatus.MANAGER_DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot save manager draft. Current status: " + status);
        }

        applyManagerReviewFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_DRAFT);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Manager review submit ─────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse submitManagerReview(Long appraisalId,
                                                 ManagerReviewRequest request,
                                                 Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireManager(appraisal, managerId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.SELF_SUBMITTED && status != AppraisalStatus.MANAGER_DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot submit manager review. Current status: " + status);
        }

        applyManagerReviewFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_REVIEWED);
        appraisalRepository.save(appraisal);

        // Notify all active HR users
        List<User> hrUsers = userRepository.findByRoleAndIsActiveTrue(Role.HR);
        for (User hr : hrUsers) {
            notificationService.send(
                    hr.getId(),
                    "Appraisal ready for approval",
                    appraisal.getEmployee().getFullName() + "'s appraisal for '"
                            + appraisal.getCycleName() + "' is ready for your approval.",
                    Type.MANAGER_REVIEW_DONE
            );
        }

        // Notify the employee
        notificationService.send(
                appraisal.getEmployee().getId(),
                "Your appraisal has been reviewed",
                "Your manager has completed their review for '"
                        + appraisal.getCycleName() + "'. Awaiting HR approval.",
                Type.MANAGER_REVIEW_DONE
        );

        return mapToResponse(appraisal);
    }

    // ── Approve ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse approveAppraisal(Long appraisalId) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        if (appraisal.getAppraisalStatus() != AppraisalStatus.MANAGER_REVIEWED) {
            throw new InvalidStatusTransitionException(
                    "Cannot approve. Current status: " + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.APPROVED);
        appraisal.setApprovedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        notificationService.send(
                appraisal.getEmployee().getId(),
                "Appraisal approved",
                "Your appraisal for '" + appraisal.getCycleName()
                        + "' has been approved. Please review and acknowledge.",
                Type.APPRAISAL_APPROVED
        );

        return mapToResponse(appraisal);
    }

    // ── Acknowledge ───────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        if (appraisal.getAppraisalStatus() != AppraisalStatus.APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot acknowledge. Current status: " + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.ACKNOWLEDGED);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Private helpers ───────────────────────────────────────────

    private void requireEmployee(Appraisal appraisal, Long employeeId) {
        if (!appraisal.getEmployee().getId().equals(employeeId))
            throw new UnauthorizedAccessException("Access denied: this is not your appraisal");
    }

    private void requireManager(Appraisal appraisal, Long managerId) {
        if (!appraisal.getManager().getId().equals(managerId))
            throw new UnauthorizedAccessException("Access denied: you are not the manager for this appraisal");
    }

    private void applySelfAssessmentFields(Appraisal appraisal, SelfAssessmentRequest request) {
        appraisal.setWhatWentWell(request.getWhatWentWell());
        appraisal.setWhatToImprove(request.getWhatToImprove());
        appraisal.setAchievements(request.getAchievements());
        appraisal.setSelfRating(request.getSelfRating());
    }

    private void applyManagerReviewFields(Appraisal appraisal, ManagerReviewRequest request) {
        appraisal.setManagerStrengths(request.getManagerStrengths());
        appraisal.setManagerImprovements(request.getManagerImprovements());
        appraisal.setManagerComments(request.getManagerComments());
        appraisal.setManagerRating(request.getManagerRating());
    }

    private Appraisal findAppraisalById(Long id) {
        return appraisalRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found with id: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private AppraisalResponse mapToResponse(Appraisal appraisal) {
        AppraisalResponse response = new AppraisalResponse();
        response.setId(appraisal.getId());
        response.setCycleName(appraisal.getCycleName());
        response.setCycleStartDate(appraisal.getCycleStartDate());
        response.setCycleEndDate(appraisal.getCycleEndDate());
        response.setCycleStatus(appraisal.getCycleStatus());
        response.setEmployeeId(appraisal.getEmployee().getId());
        response.setEmployeeName(appraisal.getEmployee().getFullName());
        response.setEmployeeJobTitle(appraisal.getEmployee().getJobTitle());
        if (appraisal.getEmployee().getDepartment() != null)
            response.setEmployeeDepartment(appraisal.getEmployee().getDepartment().getName());
        response.setManagerId(appraisal.getManager().getId());
        response.setManagerName(appraisal.getManager().getFullName());
        response.setWhatWentWell(appraisal.getWhatWentWell());
        response.setWhatToImprove(appraisal.getWhatToImprove());
        response.setAchievements(appraisal.getAchievements());
        response.setSelfRating(appraisal.getSelfRating());
        response.setManagerStrengths(appraisal.getManagerStrengths());
        response.setManagerImprovements(appraisal.getManagerImprovements());
        response.setManagerComments(appraisal.getManagerComments());
        response.setManagerRating(appraisal.getManagerRating());
        response.setAppraisalStatus(appraisal.getAppraisalStatus());
        response.setSubmittedAt(appraisal.getSubmittedAt());
        response.setApprovedAt(appraisal.getApprovedAt());
        response.setCreatedAt(appraisal.getCreatedAt());
        return response;
    }
}
