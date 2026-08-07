package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SubscriptionChargeService implements SubscriptionChargeUseCases {

    private final SubscriptionChargeRepository chargeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RegisterTransactionUseCase registerTransactionUseCase;

    public SubscriptionChargeService(SubscriptionChargeRepository chargeRepository,
                                      SubscriptionRepository subscriptionRepository,
                                      RegisterTransactionUseCase registerTransactionUseCase) {
        this.chargeRepository = chargeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
    }

    @Override
    public List<SubscriptionCharge> listarPorMes(int mes, int anio) {
        return chargeRepository.findByMesAndAnio(mes, anio);
    }

    @Override
    public SubscriptionCharge confirmar(UUID chargeId) {
        SubscriptionCharge charge = buscarCharge(chargeId);
        Subscription subscription = subscriptionRepository.findById(charge.getSubscripcionId())
                .orElseThrow(() -> new NoSuchElementException("Suscripción no encontrada: " + charge.getSubscripcionId()));

        Instant fecha = charge.getFechaEsperada().atStartOfDay(ZoneOffset.UTC).toInstant();

        Transaction transaction = registerTransactionUseCase.execute(new RegisterTransactionCommand(
                TransactionType.GASTO,
                charge.getMontoEsperado(),
                subscription.getNombre(), // nota: usa el nombre de la suscripción, ej. "Netflix"
                subscription.getCuentaId(),
                null,
                subscription.getCategoriaId(),
                null,
                subscription.getId(), // enlaza la Transaction con la suscripción
                fecha
        ));

        charge.confirmar(transaction.getId());
        return chargeRepository.save(charge);
    }

    @Override
    public SubscriptionCharge omitir(UUID chargeId) {
        SubscriptionCharge charge = buscarCharge(chargeId);
        charge.omitir();
        return chargeRepository.save(charge);
    }

    private SubscriptionCharge buscarCharge(UUID id) {
        return chargeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cargo de suscripción no encontrado: " + id));
    }
}
