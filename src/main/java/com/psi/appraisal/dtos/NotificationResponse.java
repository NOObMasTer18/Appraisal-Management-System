package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Notification.Type;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private Type type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
