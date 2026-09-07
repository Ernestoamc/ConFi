package com.confi.domain.service;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.port.out.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PeriodCloseServiceTest {

    @Test
    void publicaEventoCuandoSeCierraPeriodoPorPrimeraVez() {
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        PeriodCloseService service = new PeriodCloseService(publisher);

        service.close(YearMonth.of(2026, 8));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(captor.capture());
        DomainEvent event = captor.getValue();

        assertThat(event.eventType()).isEqualTo("period.closed");
        assertThat(event.payload().get("period")).isEqualTo("2026-08");
    }

    @Test
    void publicaEventoDeRechazoSiPeriodoYaEstabaCerrado() {
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        PeriodCloseService service = new PeriodCloseService(publisher);

        service.close(YearMonth.of(2026, 8));
        service.close(YearMonth.of(2026, 8));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher, times(2)).publish(captor.capture());
        DomainEvent rejectionEvent = captor.getAllValues().get(1);

        assertThat(rejectionEvent.eventType()).isEqualTo("period.close.rejected");
        assertThat(rejectionEvent.payload().get("reason")).isEqualTo("already-closed");
    }

    @Test
    void publicaEventoCuandoSeReabrePeriodoCerrado() {
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        PeriodCloseService service = new PeriodCloseService(publisher);

        service.close(YearMonth.of(2026, 8));
        service.reopen(YearMonth.of(2026, 8));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher, times(2)).publish(captor.capture());
        DomainEvent reopenEvent = captor.getAllValues().get(1);

        assertThat(reopenEvent.eventType()).isEqualTo("period.reopened");
        assertThat(reopenEvent.payload().get("period")).isEqualTo("2026-08");
    }

    @Test
    void publicaEventoDeRechazoAlReabrirPeriodoQueNoEstabaCerrado() {
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        PeriodCloseService service = new PeriodCloseService(publisher);

        service.reopen(YearMonth.of(2026, 8));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(publisher).publish(captor.capture());
        DomainEvent rejectionEvent = captor.getValue();

        assertThat(rejectionEvent.eventType()).isEqualTo("period.reopen.rejected");
        assertThat(rejectionEvent.payload().get("reason")).isEqualTo("not-closed");
    }

    @Test
    void listaHistorialEnOrdenDescendenteYConLimite() {
        DomainEventPublisher publisher = mock(DomainEventPublisher.class);
        PeriodCloseService service = new PeriodCloseService(publisher);

        service.close(YearMonth.of(2026, 8));
        service.close(YearMonth.of(2026, 8));
        service.reopen(YearMonth.of(2026, 8));

        List<PeriodCloseService.PeriodEventLog> events = service.listEvents(2);

        assertThat(events).hasSize(2);
        assertThat(events.get(0).eventType()).isEqualTo("period.reopened");
        assertThat(events.get(1).eventType()).isEqualTo("period.close.rejected");
    }
}
