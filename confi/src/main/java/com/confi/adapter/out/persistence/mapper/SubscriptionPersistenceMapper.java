package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.SubscriptionEntity;
import com.confi.domain.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPersistenceMapper {

    public SubscriptionEntity toEntity(Subscription subscription) {
        return new SubscriptionEntity(
                subscription.getId(),
                subscription.getNombre(),
                subscription.getMontoEstimado(),
                SubscriptionEntity.FrecuenciaJpa.valueOf(subscription.getFrecuencia().name()),
                subscription.getDiaCobro(),
                subscription.getCuentaId(),
                subscription.getCategoriaId(),
                subscription.isActiva()
        );
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.getId(),
                entity.getNombre(),
                entity.getMontoEstimado(),
                Subscription.Frecuencia.valueOf(entity.getFrecuencia().name()),
                entity.getDiaCobro(),
                entity.getCuentaId(),
                entity.getCategoriaId(),
                entity.isActiva()
        );
    }
}