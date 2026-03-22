package com.psi.appraisal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psi.appraisal.entity.Feedback;
import com.psi.appraisal.entity.Feedback.FeedbackType;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByAppraisalId(Long appraisalId);

    List<Feedback> findByRevieweeId(Long revieweeId);

    boolean existsByAppraisalIdAndReviewerIdAndFeedbackType(Long appraisalId, Long reviewerId,
            FeedbackType feedbackType);
}
