package com.confi.domain.service;

import com.confi.domain.model.Subscription;
import com.confi.domain.port.in.SubscriptionUseCases;
import com.confi.domain.port.out.SubscriptionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class SubscriptionService implements SubscriptionUseCases {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional
    public Subscription crear(CreateSubscriptionCommand command) {
        Subscription subscription = Subscription.crearNueva(
                command.nombre(), command.montoEstimado(), command.frecuencia(),
                command.diaCobro(), command.cuentaId(), command.categoriaId());
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> listarActivas() {
        return subscriptionRepository.findAllActive();
    }

    @Override
    @Transactional
    public void pausar(UUID id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Suscripción no encontrada: " + id));
        subscription.pausar();
        subscriptionRepository.save(subscription);
    }
}
