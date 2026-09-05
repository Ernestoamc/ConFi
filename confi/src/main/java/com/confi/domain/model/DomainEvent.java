package com.confi.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public DomainEvent {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType es obligatorio");
        }
        if (eventVersion < 1) {
            throw new IllegalArgumentException("eventVersion debe ser >= 1");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt es obligatorio");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload es obligatorio");
        }
    }

    public static DomainEvent create(String eventType, Map<String, Object> payload) {
        return new DomainEvent(UUID.randomUUID(), eventType, 1, Instant.now(), payload);
    }
}
