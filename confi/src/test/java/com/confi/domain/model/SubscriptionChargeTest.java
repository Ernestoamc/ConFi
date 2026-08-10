package com.confi.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionChargeTest {

    private final UUID subscripcionId = UUID.randomUUID();
    private final LocalDate fecha = LocalDate.of(2026, 8, 5);

    @Test
    void seCreaEnEstadoPendiente() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.00"));

        assertThat(charge.getEstado()).isEqualTo(SubscriptionCharge.Estado.PENDIENTE);
        assertThat(charge.getTransactionId()).isNull();
    }

    @Test
    void redondeaElMontoADosDecimales() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.999"));

        assertThat(charge.getMontoEsperado()).isEqualByComparingTo("200.00");
    }

    @Test
    void rechazaMontoCeroONegativo() {
        assertThatThrownBy(() -> SubscriptionCharge.crearPendiente(subscripcionId, fecha, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("montoEsperado");
    }

    @Test
    void confirmarLoMarcaComoConfirmadoYEnlazaLaTransaction() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.00"));
        UUID transactionId = UUID.randomUUID();

        charge.confirmar(transactionId);

        assertThat(charge.getEstado()).isEqualTo(SubscriptionCharge.Estado.CONFIRMADO);
        assertThat(charge.getTransactionId()).isEqualTo(transactionId);
    }

    @Test
    void noSePuedeConfirmarDosVeces() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.00"));
        charge.confirmar(UUID.randomUUID());

        assertThatThrownBy(() -> charge.confirmar(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    void omitirLoMarcaComoOmitido() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.00"));

        charge.omitir();

        assertThat(charge.getEstado()).isEqualTo(SubscriptionCharge.Estado.OMITIDO);
        assertThat(charge.getTransactionId()).isNull();
    }

    @Test
    void noSePuedeOmitirUnCargoYaConfirmado() {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                subscripcionId, fecha, new BigDecimal("199.00"));
        charge.confirmar(UUID.randomUUID());

        assertThatThrownBy(charge::omitir)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDIENTE");
    }
}