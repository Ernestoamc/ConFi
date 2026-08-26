package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenerateMonthlyChargesServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private SubscriptionChargeRepository chargeRepository;
    private GenerateMonthlyChargesService service;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        chargeRepository = mock(SubscriptionChargeRepository.class);
        service = new GenerateMonthlyChargesService(subscriptionRepository, chargeRepository);

        when(chargeRepository.save(any(SubscriptionCharge.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generaCargoMensualCuandoNoExisteEnElPeriodo() {
        Subscription mensual = subscription("Netflix", Subscription.Frecuencia.MENSUAL, 15);
        when(subscriptionRepository.findAllActive()).thenReturn(List.of(mensual));
        when(chargeRepository.existsBySubscripcionAndMesAnio(mensual.getId(), 8, 2026)).thenReturn(false);
        when(chargeRepository.findLastBySubscripcionId(mensual.getId())).thenReturn(Optional.empty());

        List<SubscriptionCharge> generated = service.execute(8, 2026);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getFechaEsperada()).isEqualTo(LocalDate.of(2026, 8, 15));
    }

    @Test
    void noGeneraBimestralSiSoloHaPasadoUnMesDesdeElUltimoCargo() {
        Subscription bimestral = subscription("Gym", Subscription.Frecuencia.BIMESTRAL, 10);
        SubscriptionCharge lastCharge = SubscriptionCharge.crearPendiente(
                bimestral.getId(), LocalDate.of(2026, 7, 10), new BigDecimal("499.00"));

        when(subscriptionRepository.findAllActive()).thenReturn(List.of(bimestral));
        when(chargeRepository.existsBySubscripcionAndMesAnio(bimestral.getId(), 8, 2026)).thenReturn(false);
        when(chargeRepository.findLastBySubscripcionId(bimestral.getId())).thenReturn(Optional.of(lastCharge));

        List<SubscriptionCharge> generated = service.execute(8, 2026);

        assertThat(generated).isEmpty();
        verify(chargeRepository, never()).save(any());
    }

    @Test
    void generaBimestralCuandoHanPasadoDosMesesDesdeElUltimoCargo() {
        Subscription bimestral = subscription("Gym", Subscription.Frecuencia.BIMESTRAL, 10);
        SubscriptionCharge lastCharge = SubscriptionCharge.crearPendiente(
                bimestral.getId(), LocalDate.of(2026, 6, 10), new BigDecimal("499.00"));

        when(subscriptionRepository.findAllActive()).thenReturn(List.of(bimestral));
        when(chargeRepository.existsBySubscripcionAndMesAnio(bimestral.getId(), 8, 2026)).thenReturn(false);
        when(chargeRepository.findLastBySubscripcionId(bimestral.getId())).thenReturn(Optional.of(lastCharge));

        List<SubscriptionCharge> generated = service.execute(8, 2026);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getFechaEsperada()).isEqualTo(LocalDate.of(2026, 8, 10));
    }

    @Test
    void ajustaElDiaAlUltimoDiaDelMesSiNoExisteElDiaCobro() {
        Subscription mensual = subscription("Servicio", Subscription.Frecuencia.MENSUAL, 31);

        when(subscriptionRepository.findAllActive()).thenReturn(List.of(mensual));
        when(chargeRepository.existsBySubscripcionAndMesAnio(mensual.getId(), 2, 2026)).thenReturn(false);
        when(chargeRepository.findLastBySubscripcionId(mensual.getId())).thenReturn(Optional.empty());

        List<SubscriptionCharge> generated = service.execute(2, 2026);

        assertThat(generated).hasSize(1);
        assertThat(generated.get(0).getFechaEsperada()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void lanzaErrorSiElMesEsInvalido() {
        assertThatThrownBy(() -> service.execute(13, 2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mes inválido");
    }

    private static Subscription subscription(String nombre, Subscription.Frecuencia frecuencia, int diaCobro) {
        return new Subscription(
                UUID.randomUUID(),
                nombre,
                new BigDecimal("199.90"),
                frecuencia,
                diaCobro,
                UUID.randomUUID(),
                UUID.randomUUID(),
                true
        );
    }
}
