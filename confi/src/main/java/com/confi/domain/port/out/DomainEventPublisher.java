package com.confi.domain.port.out;

import com.confi.domain.model.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
