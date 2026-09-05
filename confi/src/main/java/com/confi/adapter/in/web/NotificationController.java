package com.confi.adapter.in.web;

import com.confi.adapter.in.notifications.NotificationInbox;
import com.confi.adapter.in.notifications.NotificationItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final NotificationInbox notificationInbox;

    public NotificationController(NotificationInbox notificationInbox) {
        this.notificationInbox = notificationInbox;
    }

    @GetMapping
    public List<NotificationResponse> latest(
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(200) int limit
    ) {
        return notificationInbox.latest(limit).stream().map(NotificationController::toResponse).toList();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        return toResponse(notificationInbox.markRead(id));
    }

    @PostMapping("/read-all")
    public MarkAllReadResponse markAllRead() {
        int updated = notificationInbox.markAllRead();
        return new MarkAllReadResponse(updated);
    }

    @GetMapping("/summary")
    public NotificationSummaryResponse summary() {
        return new NotificationSummaryResponse(notificationInbox.unreadCount());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        notificationInbox.clear();
    }

    private static NotificationResponse toResponse(NotificationItem item) {
        return new NotificationResponse(
                item.id(),
                item.eventType(),
                item.occurredAt(),
                item.title(),
                item.message(),
                item.payload(),
                item.read(),
                item.readAt()
        );
    }

            public record NotificationSummaryResponse(long unreadCount) {
            }

            public record MarkAllReadResponse(int updated) {
            }

    public record NotificationResponse(
            UUID id,
            String eventType,
            Instant occurredAt,
            String title,
            String message,
                Map<String, Object> payload,
                boolean read,
                Instant readAt
    ) {
    }
}
