package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SubscriptionChargeService implements SubscriptionChargeUseCases {

    private final SubscriptionChargeRepository chargeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final DomainEventPublisher domainEventPublisher;
    private final PeriodCloseService periodCloseService;

    public SubscriptionChargeService(SubscriptionChargeRepository chargeRepository,
                                     SubscriptionRepository subscriptionRepository,
                                     RegisterTransactionUseCase registerTransactionUseCase,
                                     DomainEventPublisher domainEventPublisher) {
        this(chargeRepository, subscriptionRepository, registerTransactionUseCase, domainEventPublisher, null);
    }

    public SubscriptionChargeService(SubscriptionChargeRepository chargeRepository,
                                     SubscriptionRepository subscriptionRepository,
                                     RegisterTransactionUseCase registerTransactionUseCase,
                                     DomainEventPublisher domainEventPublisher,
                                     PeriodCloseService periodCloseService) {
        this.chargeRepository = chargeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.domainEventPublisher = domainEventPublisher;
        this.periodCloseService = periodCloseService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionCharge> listarPorMes(int mes, int anio) {
        return chargeRepository.findByMesAndAnio(mes, anio);
    }

    @Override
    @Transactional
    public SubscriptionCharge confirmar(UUID chargeId) {
        SubscriptionCharge charge = buscarCharge(chargeId);
        validarPeriodoAbierto(charge, "confirmacion de cargo de suscripcion");
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
        SubscriptionCharge saved = chargeRepository.save(charge);
        publicarEventoCargoConfirmado(saved, subscription, transaction);
        return saved;
    }

    @Override
    @Transactional
    public SubscriptionCharge omitir(UUID chargeId) {
        SubscriptionCharge charge = buscarCharge(chargeId);
        validarPeriodoAbierto(charge, "omision de cargo de suscripcion");
        charge.omitir();
        return chargeRepository.save(charge);
    }

    private SubscriptionCharge buscarCharge(UUID id) {
        return chargeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cargo de suscripción no encontrado: " + id));
    }

    private void publicarEventoCargoConfirmado(SubscriptionCharge charge, Subscription subscription, Transaction transaction) {
        DomainEvent event = DomainEvent.create("subscription.charge.confirmed", Map.of(
                "chargeId", charge.getId().toString(),
                "subscriptionId", subscription.getId().toString(),
                "transactionId", transaction.getId().toString(),
                "accountId", subscription.getCuentaId().toString(),
                "amount", charge.getMontoEsperado().toPlainString(),
                "confirmedAt", Instant.now().toString()
        ));
        domainEventPublisher.publish(event);
    }

    private void validarPeriodoAbierto(SubscriptionCharge charge, String context) {
        if (periodCloseService == null) {
            return;
        }
        Instant instant = charge.getFechaEsperada().atStartOfDay(ZoneOffset.UTC).toInstant();
        periodCloseService.ensureOpen(instant, context);
    }
}
