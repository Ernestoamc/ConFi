package com.confi.domain.service;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "app.events.reminders", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionDueSoonReminderService {

    private final SubscriptionChargeRepository chargeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DomainEventPublisher eventPublisher;
    private final int daysAhead;
    private final ConcurrentHashMap<String, Boolean> emitted = new ConcurrentHashMap<>();

    public SubscriptionDueSoonReminderService(SubscriptionChargeRepository chargeRepository,
                                              SubscriptionRepository subscriptionRepository,
                                              DomainEventPublisher eventPublisher,
                                              @Value("${app.events.reminders.days-ahead:3}") int daysAhead) {
        this.chargeRepository = chargeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.daysAhead = daysAhead;
    }

    public SubscriptionDueSoonReminderService(SubscriptionChargeRepository chargeRepository,
                                              SubscriptionRepository subscriptionRepository,
                                              DomainEventPublisher eventPublisher) {
        this(chargeRepository, subscriptionRepository, eventPublisher, 3);
    }

    @Scheduled(cron = "${app.events.reminders.cron:0 0 8 * * *}")
    public void publishDueSoonReminders() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate until = today.plusDays(daysAhead);

        List<SubscriptionCharge> charges = chargeRepository.findByMesAndAnio(today.getMonthValue(), today.getYear());
        for (SubscriptionCharge charge : charges) {
            if (charge.getEstado() != SubscriptionCharge.Estado.PENDIENTE) {
                continue;
            }
            if (charge.getFechaEsperada().isBefore(today) || charge.getFechaEsperada().isAfter(until)) {
                continue;
            }

            Subscription subscription = subscriptionRepository.findById(charge.getSubscripcionId()).orElse(null);
            String key = charge.getId() + ":" + today;
            if (subscription == null || emitted.putIfAbsent(key, true) != null) {
                continue;
            }

            eventPublisher.publish(DomainEvent.create("subscription.charge.due.soon", Map.of(
                    "chargeId", charge.getId().toString(),
                    "subscriptionId", subscription.getId().toString(),
                    "subscriptionName", subscription.getNombre(),
                    "accountId", subscription.getCuentaId().toString(),
                    "amount", charge.getMontoEsperado().toPlainString(),
                    "dueDate", charge.getFechaEsperada().toString(),
                    "daysAhead", daysAhead
            )));
        }
    }
}
