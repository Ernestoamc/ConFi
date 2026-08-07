package com.confi.domain.port.out;

import com.confi.domain.model.SubscriptionCharge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionChargeRepository {

    SubscriptionCharge save(SubscriptionCharge charge);
    Optional<SubscriptionCharge> findById(UUID id);
    List<SubscriptionCharge> findByMesAndAnio(int mes, int anio);
    boolean existsBySubscripcionAndMesAnio(UUID subscripcionId, int mes, int anio);
}