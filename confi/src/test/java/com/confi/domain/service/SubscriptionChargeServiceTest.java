package com.confi.domain.service;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class SubscriptionChargeServiceTest {

    @Test
    void bloqueaConfirmacionSiPeriodoEstaCerrado() {
        SubscriptionChargeRepository chargeRepository = mock(SubscriptionChargeRepository.class);
        SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
        RegisterTransactionUseCase registerTransactionUseCase = mock(RegisterTransactionUseCase.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);
        PeriodCloseService periodCloseService = mock(PeriodCloseService.class);

        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 15),
                new BigDecimal("249.00")
        );

        when(chargeRepository.findById(charge.getId())).thenReturn(Optional.of(charge));
        doThrow(new IllegalStateException("Periodo cerrado"))
                .when(periodCloseService)
                .ensureOpen(any(), eq("confirmacion de cargo de suscripcion"));

        SubscriptionChargeService service = new SubscriptionChargeService(
                chargeRepository,
                subscriptionRepository,
                registerTransactionUseCase,
                eventPublisher,
                periodCloseService
        );

        assertThatThrownBy(() -> service.confirmar(charge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Periodo cerrado");

        verify(registerTransactionUseCase, never()).execute(any());
    }

    @Test
    void publicaEventoCuandoSeOmiteUnCargo() {
        SubscriptionChargeRepository chargeRepository = mock(SubscriptionChargeRepository.class);
        SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
        RegisterTransactionUseCase registerTransactionUseCase = mock(RegisterTransactionUseCase.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);

        UUID subId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subId,
                LocalDate.of(2026, 9, 20),
                new BigDecimal("120.00")
        );

        Subscription subscription = new Subscription(
                subId,
                "Servicio X",
                new BigDecimal("120.00"),
                Subscription.Frecuencia.MENSUAL,
                20,
                accountId,
                categoryId,
                true
        );

        when(chargeRepository.findById(charge.getId())).thenReturn(Optional.of(charge));
        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        when(chargeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionChargeService service = new SubscriptionChargeService(
                chargeRepository,
                subscriptionRepository,
                registerTransactionUseCase,
                eventPublisher
        );

        service.omitir(charge.getId());

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        DomainEvent event = captor.getValue();

        assertThat(event.eventType()).isEqualTo("subscription.charge.skipped");
        assertThat(event.payload().get("chargeId")).isEqualTo(charge.getId().toString());
    }
}
