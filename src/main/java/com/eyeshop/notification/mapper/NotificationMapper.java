package com.eyeshop.notification.mapper;

import com.eyeshop.notification.dto.response.NotificationResponse;
import com.eyeshop.notification.entity.Notification;

public final class NotificationMapper {
    private NotificationMapper() {}

    public static NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return new NotificationResponse(
            n.getId(), n.getTitle(), n.getMessage(),
            n.getType(), n.isRead(), n.getCreatedAt()
        );
    }
}