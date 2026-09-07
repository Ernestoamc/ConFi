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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final int maxDaysAhead;
    private final java.math.BigDecimal highAmountThreshold;
    private final ConcurrentHashMap<String, Boolean> emitted = new ConcurrentHashMap<>();

    public SubscriptionDueSoonReminderService(SubscriptionChargeRepository chargeRepository,
                                              SubscriptionRepository subscriptionRepository,
                                              DomainEventPublisher eventPublisher,
                                              @Value("${app.events.reminders.days-ahead:3}") int daysAhead,
                                              @Value("${app.events.reminders.max-days-ahead:15}") int maxDaysAhead,
                                              @Value("${app.events.reminders.high-amount-threshold:1000}") java.math.BigDecimal highAmountThreshold) {
        this.chargeRepository = chargeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.daysAhead = daysAhead;
        this.maxDaysAhead = maxDaysAhead;
        this.highAmountThreshold = highAmountThreshold;
    }

    public SubscriptionDueSoonReminderService(SubscriptionChargeRepository chargeRepository,
                                              SubscriptionRepository subscriptionRepository,
                                              DomainEventPublisher eventPublisher) {
        this(chargeRepository, subscriptionRepository, eventPublisher, 3, 15, new java.math.BigDecimal("1000"));
    }

    public SubscriptionDueSoonReminderService(SubscriptionChargeRepository chargeRepository,
                                              SubscriptionRepository subscriptionRepository,
                                              DomainEventPublisher eventPublisher,
                                              int daysAhead,
                                              int maxDaysAhead,
                                              java.math.BigDecimal highAmountThreshold) {
        this.chargeRepository = chargeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.daysAhead = daysAhead;
        this.maxDaysAhead = maxDaysAhead;
        this.highAmountThreshold = highAmountThreshold;
    }

    @Scheduled(cron = "${app.events.reminders.cron:0 0 8 * * *}")
    public void publishDueSoonReminders() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<SubscriptionCharge> charges = findChargesForWindow(today);
        for (SubscriptionCharge charge : charges) {
            if (charge.getEstado() != SubscriptionCharge.Estado.PENDIENTE) {
                continue;
            }

            Subscription subscription = subscriptionRepository.findById(charge.getSubscripcionId()).orElse(null);
            if (subscription == null) {
                continue;
            }

            int daysUntilDue = (int) java.time.temporal.ChronoUnit.DAYS.between(today, charge.getFechaEsperada());
            int adaptiveWindowDays = calcularVentanaAdaptativa(subscription, charge);
            if (daysUntilDue < 0 || daysUntilDue > adaptiveWindowDays) {
                continue;
            }

            String priority = calcularPrioridad(daysUntilDue, charge);
            String key = charge.getId() + ":" + today;
            if (emitted.putIfAbsent(key, true) != null) {
                continue;
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("chargeId", charge.getId().toString());
            payload.put("subscriptionId", subscription.getId().toString());
            payload.put("subscriptionName", subscription.getNombre());
            payload.put("accountId", subscription.getCuentaId().toString());
            payload.put("amount", charge.getMontoEsperado().toPlainString());
            payload.put("dueDate", charge.getFechaEsperada().toString());
            payload.put("daysAhead", daysAhead);
            payload.put("adaptiveWindowDays", adaptiveWindowDays);
            payload.put("daysUntilDue", daysUntilDue);
            payload.put("priority", priority);
            payload.put("reminderMode", "adaptive");
            eventPublisher.publish(DomainEvent.create("subscription.charge.due.soon", payload));
        }
    }

    private List<SubscriptionCharge> findChargesForWindow(LocalDate today) {
        List<SubscriptionCharge> charges = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(today);
        charges.addAll(chargeRepository.findByMesAndAnio(currentMonth.getMonthValue(), currentMonth.getYear()));

        YearMonth nextMonth = currentMonth.plusMonths(1);
        charges.addAll(chargeRepository.findByMesAndAnio(nextMonth.getMonthValue(), nextMonth.getYear()));
        return charges;
    }

    private int calcularVentanaAdaptativa(Subscription subscription, SubscriptionCharge charge) {
        int extraPorFrecuencia = switch (subscription.getFrecuencia()) {
            case MENSUAL -> 0;
            case BIMESTRAL -> 2;
            case ANUAL -> 4;
        };

        int extraPorMonto = charge.getMontoEsperado().compareTo(highAmountThreshold) >= 0 ? 2 : 0;
        int window = daysAhead + extraPorFrecuencia + extraPorMonto;
        return Math.max(1, Math.min(window, maxDaysAhead));
    }

    private String calcularPrioridad(int daysUntilDue, SubscriptionCharge charge) {
        if (daysUntilDue <= 0) {
            return "CRITICAL";
        }
        if (daysUntilDue <= 1 || charge.getMontoEsperado().compareTo(highAmountThreshold) >= 0) {
            return "HIGH";
        }
        if (daysUntilDue <= 3) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
