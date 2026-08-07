package com.confi.domain.port.in;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.Subscription.Frecuencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SubscriptionUseCases {

    Subscription crear(CreateSubscriptionCommand command);
    List<Subscription> listarActivas();
    void pausar(UUID id);

    record CreateSubscriptionCommand(
            String nombre,
            BigDecimal montoEstimado,
            Frecuencia frecuencia,
            int diaCobro,
            UUID cuentaId,
            UUID categoriaId
    ) {}
}