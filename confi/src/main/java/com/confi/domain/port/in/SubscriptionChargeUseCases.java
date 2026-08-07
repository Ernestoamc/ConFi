package com.confi.domain.port.in;

import com.confi.domain.model.SubscriptionCharge;

import java.util.List;
import java.util.UUID;

public interface SubscriptionChargeUseCases {

    List<SubscriptionCharge> listarPorMes(int mes, int anio);
    SubscriptionCharge confirmar(UUID chargeId);
    SubscriptionCharge omitir(UUID chargeId);
}