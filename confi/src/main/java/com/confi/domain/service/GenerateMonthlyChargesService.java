package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class GenerateMonthlyChargesService implements GenerateMonthlyChargesUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionChargeRepository chargeRepository;

    public GenerateMonthlyChargesService(SubscriptionRepository subscriptionRepository,
                                          SubscriptionChargeRepository chargeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.chargeRepository = chargeRepository;
    }

    @Override
    public List<SubscriptionCharge> execute(int mes, int anio) {
        List<SubscriptionCharge> generados = new ArrayList<>();
        YearMonth periodo = YearMonth.of(anio, mes);

        for (Subscription subscription : subscriptionRepository.findAllActive()) {
            boolean yaExiste = chargeRepository.existsBySubscripcionAndMesAnio(subscription.getId(), mes, anio);
            if (yaExiste) {
                continue; // idempotente: no duplicar cargos si ya se generó este mes
            }

            // Si el día de cobro no existe en este mes (ej. 31 en febrero), usa el último día del mes.
            int dia = Math.min(subscription.getDiaCobro(), periodo.lengthOfMonth());
            LocalDate fechaEsperada = periodo.atDay(dia);

            SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                    subscription.getId(), fechaEsperada, subscription.getMontoEstimado());
            generados.add(chargeRepository.save(charge));
        }

        return generados;
    }
}
