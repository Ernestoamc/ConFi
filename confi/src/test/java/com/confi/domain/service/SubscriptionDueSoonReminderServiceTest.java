package com.confi.domain.service;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SubscriptionDueSoonReminderServiceTest {

    @Test
    void publicaRecordatorioParaCargoPendienteProximoAVencer() {
        SubscriptionChargeRepository chargeRepository = mock(SubscriptionChargeRepository.class);
        SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);

        SubscriptionDueSoonReminderService service = new SubscriptionDueSoonReminderService(
                                chargeRepository, subscriptionRepository, eventPublisher, 3, 15, new BigDecimal("1000"));

        UUID subId = UUID.randomUUID();
        Subscription sub = new Subscription(subId, "Netflix", new BigDecimal("249.00"),
                Subscription.Frecuencia.MENSUAL, 15, UUID.randomUUID(), UUID.randomUUID(), true);

        SubscriptionCharge dueSoon = SubscriptionCharge.crearPendiente(
                subId,
                LocalDate.now(java.time.ZoneOffset.UTC).plusDays(1),
                new BigDecimal("249.00")
        );

        when(chargeRepository.findByMesAndAnio(anyInt(), anyInt())).thenReturn(List.of(dueSoon));
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        service.publishDueSoonReminders();

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo("subscription.charge.due.soon");
        assertThat(captor.getValue().payload().get("reminderMode")).isEqualTo("adaptive");
        assertThat(captor.getValue().payload().get("priority")).isNotNull();
    }

    @Test
    void publicaRecordatorioParaCargoAnualConVentanaAdaptativaMayor() {
        SubscriptionChargeRepository chargeRepository = mock(SubscriptionChargeRepository.class);
        SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);

        SubscriptionDueSoonReminderService service = new SubscriptionDueSoonReminderService(
                chargeRepository, subscriptionRepository, eventPublisher, 3, 15, new BigDecimal("1000"));

        UUID subId = UUID.randomUUID();
        Subscription sub = new Subscription(subId, "Seguro anual", new BigDecimal("1500.00"),
                Subscription.Frecuencia.ANUAL, 15, UUID.randomUUID(), UUID.randomUUID(), true);

        SubscriptionCharge adaptiveDue = SubscriptionCharge.crearPendiente(
                subId,
                LocalDate.now(java.time.ZoneOffset.UTC).plusDays(6),
                new BigDecimal("1500.00")
        );

        when(chargeRepository.findByMesAndAnio(anyInt(), anyInt())).thenReturn(List.of(adaptiveDue));
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));

        service.publishDueSoonReminders();

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().payload().get("adaptiveWindowDays")).isEqualTo(9);
                assertThat(captor.getValue().payload().get("daysUntilDue")).isEqualTo(6);
        assertThat(captor.getValue().payload().get("priority")).isEqualTo("HIGH");
    }
}
