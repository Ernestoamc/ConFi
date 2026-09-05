package com.confi.adapter.in.notifications;

import com.confi.domain.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.events.kafka", name = "enabled", havingValue = "true")
public class KafkaDomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventListener.class);
    private static final JsonParser JSON_PARSER = JsonParserFactory.getJsonParser();

    private final NotificationInbox notificationInbox;

    public KafkaDomainEventListener(NotificationInbox notificationInbox) {
        this.notificationInbox = notificationInbox;
    }

    @KafkaListener(
            topics = "${app.events.kafka.topic:confi.events}",
            groupId = "${app.events.kafka.group-id:confi.notification.local}"
    )
    public void onMessage(String rawMessage) {
        try {
            DomainEvent event = toDomainEvent(rawMessage);
            notificationInbox.add(toNotification(event));
        } catch (Exception ex) {
            log.warn("No se pudo procesar mensaje de Kafka en notification listener", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private DomainEvent toDomainEvent(String rawMessage) {
        Map<String, Object> map = JSON_PARSER.parseMap(rawMessage);
        String eventId = String.valueOf(map.get("eventId"));
        String eventType = String.valueOf(map.get("eventType"));
        Object rawVersion = map.get("eventVersion");
        int eventVersion = rawVersion instanceof Number ? ((Number) rawVersion).intValue() : 1;
        String occurredAt = String.valueOf(map.get("occurredAt"));
        Map<String, Object> payload = (Map<String, Object>) map.getOrDefault("payload", Map.of());

        return new DomainEvent(
                UUID.fromString(eventId),
                eventType,
                eventVersion,
                Instant.parse(occurredAt),
                payload
        );
    }

    private NotificationItem toNotification(DomainEvent event) {
        String title = switch (event.eventType()) {
            case "transaction.created" -> "Nueva transaccion";
            case "subscription.charge.generated" -> "Cargo de suscripcion generado";
            case "subscription.charge.confirmed" -> "Cargo de suscripcion confirmado";
            case "account.low.balance" -> "Alerta de saldo bajo";
            case "budget.threshold.exceeded" -> "Alerta de presupuesto excedido";
            case "subscription.charge.due.soon" -> "Recordatorio de vencimiento";
            default -> "Evento de negocio";
        };

        String message = buildMessage(event.eventType(), event.payload());
        return new NotificationItem(
                UUID.randomUUID(),
                event.eventType(),
                event.occurredAt() != null ? event.occurredAt() : Instant.now(),
                title,
                message,
            event.payload(),
            false,
            null
        );
    }

    private String buildMessage(String eventType, Map<String, Object> payload) {
        if ("transaction.created".equals(eventType)) {
            Object amount = payload.get("amount");
            Object type = payload.get("type");
            return "Se registro una transaccion " + type + " por " + amount;
        }
        if ("subscription.charge.generated".equals(eventType)) {
            Object amount = payload.get("amount");
            Object dueDate = payload.get("dueDate");
            return "Se genero un cargo por " + amount + " con vencimiento " + dueDate;
        }
        if ("subscription.charge.confirmed".equals(eventType)) {
            Object amount = payload.get("amount");
            return "Se confirmo un cargo de suscripcion por " + amount;
        }
        if ("account.low.balance".equals(eventType)) {
            Object accountName = payload.get("accountName");
            Object balance = payload.get("balance");
            Object threshold = payload.get("threshold");
            return "La cuenta " + accountName + " tiene saldo bajo: " + balance + " (umbral " + threshold + ")";
        }
        if ("budget.threshold.exceeded".equals(eventType)) {
            Object actual = payload.get("actual");
            Object planned = payload.get("planned");
            Object difference = payload.get("difference");
            return "Presupuesto excedido: real " + actual + " vs planeado " + planned + " (exceso " + difference + ")";
        }
        if ("subscription.charge.due.soon".equals(eventType)) {
            Object name = payload.get("subscriptionName");
            Object dueDate = payload.get("dueDate");
            Object amount = payload.get("amount");
            return "Recordatorio: " + name + " vence el " + dueDate + " por " + amount;
        }
        return "Se recibio el evento " + eventType;
    }
}
