package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.FeedbackRequest;
import com.psi.appraisal.dtos.FeedbackResponse;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Feedback;
import com.psi.appraisal.entity.Notification.Type;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.FeedbackRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.FeedbackService;
import com.psi.appraisal.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request, Long reviewerId) {
        Appraisal appraisal = appraisalRepository.findById(request.getAppraisalId())
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new RuntimeException("Reviewee not found"));

        // Guard: prevent duplicate feedback of same type from same reviewer
        if (feedbackRepository.existsByAppraisalIdAndReviewerIdAndFeedbackType(
                request.getAppraisalId(), reviewerId, request.getFeedbackType())) {
            throw new RuntimeException("You have already submitted "
                    + request.getFeedbackType() + " feedback for this appraisal");
        }

        Feedback feedback = Feedback.builder()
                .appraisal(appraisal)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .comments(request.getComments())
                .rating(request.getRating())
                .feedbackType(request.getFeedbackType())
                .build();

        feedbackRepository.save(feedback);

        // Notify the reviewee that feedback was submitted about them
        notificationService.send(
                reviewee.getId(),
                "New feedback received",
                reviewer.getFullName() + " has submitted " + request.getFeedbackType()
                        + " feedback for your appraisal.",
                Type.FEEDBACK_RECEIVED
        );

        return mapToResponse(feedback);
    }

    @Override
    public List<FeedbackResponse> getFeedbackByAppraisal(Long appraisalId) {
        return feedbackRepository.findByAppraisalId(appraisalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getFeedbackForEmployee(Long employeeId) {
        return feedbackRepository.findByRevieweeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        FeedbackResponse response = modelMapper.map(feedback, FeedbackResponse.class);
        response.setAppraisalId(feedback.getAppraisal().getId());
        response.setReviewerId(feedback.getReviewer().getId());
        response.setReviewerName(feedback.getReviewer().getFullName());
        response.setRevieweeId(feedback.getReviewee().getId());
        response.setRevieweeName(feedback.getReviewee().getFullName());
        return response;
    }
}
