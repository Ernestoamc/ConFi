package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.model.DomainEvent;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateMonthlyChargesService implements GenerateMonthlyChargesUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionChargeRepository chargeRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final PeriodCloseService periodCloseService;

    public GenerateMonthlyChargesService(SubscriptionRepository subscriptionRepository,
                                         SubscriptionChargeRepository chargeRepository,
                                         DomainEventPublisher domainEventPublisher) {
        this(subscriptionRepository, chargeRepository, domainEventPublisher, null);
    }

    public GenerateMonthlyChargesService(SubscriptionRepository subscriptionRepository,
                                         SubscriptionChargeRepository chargeRepository,
                                         DomainEventPublisher domainEventPublisher,
                                         PeriodCloseService periodCloseService) {
        this.subscriptionRepository = subscriptionRepository;
        this.chargeRepository = chargeRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.periodCloseService = periodCloseService;
    }

    @Override
    @Transactional
    public List<SubscriptionCharge> execute(int mes, int anio) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes inválido: " + mes);
        }
        if (anio < 1900) {
            throw new IllegalArgumentException("Año inválido: " + anio);
        }

        if (periodCloseService != null) {
            Instant periodStart = YearMonth.of(anio, mes).atDay(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            periodCloseService.ensureOpen(periodStart, "generacion de cargos de suscripcion");
        }

        List<SubscriptionCharge> generados = new ArrayList<>();
        YearMonth periodo = YearMonth.of(anio, mes);

        for (Subscription subscription : subscriptionRepository.findAllActive()) {
            boolean yaExiste = chargeRepository.existsBySubscripcionAndMesAnio(subscription.getId(), mes, anio);
            if (yaExiste) {
                continue; // idempotente: no duplicar cargos si ya se generó este mes
            }

            if (!debeGenerarSegunFrecuencia(subscription, periodo)) {
                continue;
            }

            // Si el día de cobro no existe en este mes (ej. 31 en febrero), usa el último día del mes.
            int dia = Math.min(subscription.getDiaCobro(), periodo.lengthOfMonth());
            LocalDate fechaEsperada = periodo.atDay(dia);

            SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                    subscription.getId(), fechaEsperada, subscription.getMontoEstimado());
                SubscriptionCharge saved = chargeRepository.save(charge);
                publicarEventoCargoGenerado(saved, subscription, mes, anio);
                generados.add(saved);
        }

        return generados;
    }

    private void publicarEventoCargoGenerado(SubscriptionCharge charge, Subscription subscription, int mes, int anio) {
        DomainEvent event = DomainEvent.create("subscription.charge.generated", Map.of(
                "chargeId", charge.getId().toString(),
                "subscriptionId", subscription.getId().toString(),
                "accountId", subscription.getCuentaId().toString(),
                "amount", charge.getMontoEsperado().toPlainString(),
                "dueDate", charge.getFechaEsperada().toString(),
                "periodMonth", mes,
                "periodYear", anio
        ));
        domainEventPublisher.publish(event);
    }

    private boolean debeGenerarSegunFrecuencia(Subscription subscription, YearMonth periodoActual) {
        return chargeRepository.findLastBySubscripcionId(subscription.getId())
                .map(last -> {
                    YearMonth ultimoPeriodo = YearMonth.from(last.getFechaEsperada());
                    int monthsElapsed = monthsBetween(ultimoPeriodo, periodoActual);
                    return monthsElapsed >= mesesRequeridos(subscription.getFrecuencia());
                })
                .orElse(true);
    }

    private static int monthsBetween(YearMonth from, YearMonth to) {
        if (to.isBefore(from)) {
            return 0;
        }
        Period period = Period.between(from.atDay(1), to.atDay(1));
        return period.getYears() * 12 + period.getMonths();
    }

    private static int mesesRequeridos(Subscription.Frecuencia frecuencia) {
        return switch (frecuencia) {
            case MENSUAL -> 1;
            case BIMESTRAL -> 2;
            case ANUAL -> 12;
        };
    }
}
