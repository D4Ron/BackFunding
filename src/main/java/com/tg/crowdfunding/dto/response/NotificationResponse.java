package com.tg.crowdfunding.dto.response;

import com.tg.crowdfunding.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    private boolean lu;
    private LocalDateTime createdAt;
}