package com.psi.appraisal.services.impl;

import com.psi.appraisal.config.EmailTemplateService;
import com.psi.appraisal.dtos.NotificationResponse;
import com.psi.appraisal.entity.Notification;
import com.psi.appraisal.entity.Notification.Type;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.exception.ResourceNotFoundException;
import com.psi.appraisal.exception.UnauthorizedAccessException;
import com.psi.appraisal.repository.NotificationRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.EmailService;
import com.psi.appraisal.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final ModelMapper modelMapper;

    @Override
    public void send(Long userId, String title, String message, Type type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 1. Save in-app notification
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // 2. Send email — runs async in background, won't block API response
        sendEmailForType(user, title, message, type);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> modelMapper.map(n, NotificationResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Access denied: this is not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return modelMapper.map(notification, NotificationResponse.class);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ── Email dispatcher ──────────────────────────────────────────
    // Matches the notification type to the right email template
    private void sendEmailForType(User user, String title, String message, Type type) {
        try {
            String htmlBody = switch (type) {
                case CYCLE_STARTED ->
                        emailTemplateService.cycleStarted(
                                user.getFullName(),
                                extractCycleName(message),
                                "", ""   // dates not in message — send generic version
                        );
                case SELF_ASSESSMENT_SUBMITTED ->
                        emailTemplateService.selfAssessmentSubmitted(
                                user.getFullName(),
                                extractEmployeeName(message),
                                extractCycleName(message)
                        );
                case MANAGER_REVIEW_DONE ->
                        emailTemplateService.managerReviewDone(
                                user.getFullName(),
                                extractCycleName(message)
                        );
                case APPRAISAL_APPROVED ->
                        emailTemplateService.appraisalApproved(
                                user.getFullName(),
                                extractCycleName(message)
                        );
                case FEEDBACK_RECEIVED ->
                        emailTemplateService.feedbackReceived(
                                user.getFullName(),
                                extractReviewerName(message),
                                extractFeedbackType(message)
                        );
                default ->
                        buildGenericEmail(user.getFullName(), title, message);
            };

            emailService.sendHtmlEmail(user.getEmail(), title, htmlBody);

        } catch (Exception e) {
            // Email failure must never crash the notification flow
            log.error("Email dispatch failed for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ── Simple message parsers ────────────────────────────────────
    // These extract key info from the message string we pass to send()
    // e.g. "Your appraisal for 'Q1 2025' has been created"

    private String extractCycleName(String message) {
        try {
            int start = message.indexOf("'") + 1;
            int end   = message.indexOf("'", start);
            if (start > 0 && end > start) return message.substring(start, end);
        } catch (Exception ignored) {}
        return "";
    }

    private String extractEmployeeName(String message) {
        // "Alice Employee has submitted..." → "Alice Employee"
        try {
            return message.split(" has ")[0].trim();
        } catch (Exception ignored) {}
        return "";
    }

    private String extractReviewerName(String message) {
        // "John Manager has submitted PEER feedback..." → "John Manager"
        try {
            return message.split(" has ")[0].trim();
        } catch (Exception ignored) {}
        return "";
    }

    private String extractFeedbackType(String message) {
        // "...submitted peer feedback..." → "peer"
        try {
            String lower = message.toLowerCase();
            if (lower.contains("peer"))    return "PEER";
            if (lower.contains("manager")) return "MANAGER";
            if (lower.contains("self"))    return "SELF";
        } catch (Exception ignored) {}
        return "peer";
    }

    private String buildGenericEmail(String name, String title, String message) {
        return """
            <html><body style="font-family:sans-serif;padding:32px;background:#f5f5f5;">
              <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;padding:32px;">
                <h2 style="color:#7c3aed;">%s</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>%s</p>
                <p style="color:#9ca3af;font-size:12px;margin-top:32px;">
                  This is an automated message from Appraisal System.
                </p>
              </div>
            </body></html>
            """.formatted(title, name, message);
    }
}
