package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.SubscriptionChargeEntity;
import com.confi.domain.model.SubscriptionCharge;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionChargePersistenceMapper {

    public SubscriptionChargeEntity toEntity(SubscriptionCharge charge) {
        return new SubscriptionChargeEntity(
                charge.getId(),
                charge.getSubscripcionId(),
                charge.getFechaEsperada(),
                charge.getMontoEsperado(),
                SubscriptionChargeEntity.EstadoJpa.valueOf(charge.getEstado().name()),
                charge.getTransactionId()
        );
    }

    public SubscriptionCharge toDomain(SubscriptionChargeEntity entity) {
        return new SubscriptionCharge(
                entity.getId(),
                entity.getSubscripcionId(),
                entity.getFechaEsperada(),
                entity.getMontoEsperado(),
                SubscriptionCharge.Estado.valueOf(entity.getEstado().name()),
                entity.getTransactionId()
        );
    }
}