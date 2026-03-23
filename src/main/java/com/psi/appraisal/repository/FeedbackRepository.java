package com.psi.appraisal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.psi.appraisal.entity.Feedback;
import com.psi.appraisal.entity.Feedback.FeedbackType;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("""
            select f
            from Feedback f
            join fetch f.reviewer
            join fetch f.reviewee
            join fetch f.appraisal
            where f.appraisal.id = :appraisalId
            """)
    List<Feedback> findByAppraisalId(@Param("appraisalId") Long appraisalId);

    @Query("""
            select f
            from Feedback f
            join fetch f.reviewer
            join fetch f.reviewee
            join fetch f.appraisal
            where f.reviewee.id = :revieweeId
            """)
    List<Feedback> findByRevieweeId(@Param("revieweeId") Long revieweeId);

    boolean existsByAppraisalIdAndReviewerIdAndFeedbackType(Long appraisalId, Long reviewerId,
            FeedbackType feedbackType);
}
