package com.eyeshop.notification.service;

import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface NotificationService {
    void sendNotification(UUID userId, String title, String message, String type);
    PageResponse<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);
    long getUnreadCount(UUID userId);
    void markAsRead(Long notificationId, UUID userId);
    void markAllAsRead(UUID userId);
}