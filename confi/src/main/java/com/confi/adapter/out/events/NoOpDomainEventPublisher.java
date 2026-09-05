package com.confi.adapter.out.events;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.events.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.debug("Kafka deshabilitado; evento omitido: {}", event.eventType());
    }
}
