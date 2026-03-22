package com.psi.appraisal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psi.appraisal.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
 
    // All notifications for a user, newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
 
    // Only unread notifications
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
 
    // Count unread (for a badge counter on the frontend)
    long countByUserIdAndIsReadFalse(Long userId);
}
 

