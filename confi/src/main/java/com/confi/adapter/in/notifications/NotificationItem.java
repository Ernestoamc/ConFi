package com.confi.adapter.in.notifications;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationItem(
        UUID id,
        String eventType,
        Instant occurredAt,
        String title,
        String message,
                Map<String, Object> payload,
                boolean read,
                Instant readAt
) {

        public NotificationItem markAsRead(Instant at) {
                if (read) {
                        return this;
                }
                return new NotificationItem(id, eventType, occurredAt, title, message, payload, true, at);
        }
}
