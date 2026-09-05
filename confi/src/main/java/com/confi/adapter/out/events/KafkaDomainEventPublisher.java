package com.confi.adapter.out.events;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.events.kafka", name = "enabled", havingValue = "true")
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     @Value("${app.events.kafka.topic:confi.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String payload = toJson(event);
            kafkaTemplate.send(topic, event.eventType(), payload);
        } catch (Exception ex) {
            // No bloquear transacciones por errores de infraestructura de notificaciones.
            log.warn("No se pudo publicar evento {} en Kafka", event.eventType(), ex);
        }
    }

    private String toJson(DomainEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        appendField(sb, "eventId", event.eventId().toString(), true);
        appendField(sb, "eventType", event.eventType(), true);
        appendField(sb, "eventVersion", event.eventVersion(), true);
        appendField(sb, "occurredAt", event.occurredAt().toString(), true);
        sb.append('"').append("payload").append('"').append(':').append(toJsonValue(event.payload()));
        sb.append('}');
        return sb.toString();
    }

    private void appendField(StringBuilder sb, String name, Object value, boolean withComma) {
        sb.append('"').append(escape(name)).append('"').append(':').append(toJsonValue(value));
        if (withComma) {
            sb.append(',');
        }
    }

    @SuppressWarnings("unchecked")
    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append('"')
                        .append(':')
                        .append(toJsonValue(entry.getValue()));
            }
            sb.append('}');
            return sb.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(toJsonValue(item));
            }
            sb.append(']');
            return sb.toString();
        }
        return '"' + escape(value.toString()) + '"';
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
