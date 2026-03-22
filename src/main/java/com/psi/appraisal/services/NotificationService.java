package com.psi.appraisal.services;


import java.util.List;

import com.psi.appraisal.dtos.NotificationResponse;
import com.psi.appraisal.entity.Notification.Type;;

public interface NotificationService {

    void send(Long userId, String title, String message, Type type);

    List<NotificationResponse> getMyNotifications(Long userId);

    NotificationResponse markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long countUnread(Long userId);
}
