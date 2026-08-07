package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.SubscriptionEntity;
import com.confi.adapter.out.persistence.mapper.SubscriptionPersistenceMapper;
import com.confi.domain.model.Subscription;
import com.confi.domain.port.out.SubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionPersistenceMapper mapper;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository jpaRepository, SubscriptionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity saved = jpaRepository.save(mapper.toEntity(subscription));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findAllActive() {
        return jpaRepository.findByActivaTrue().stream().map(mapper::toDomain).toList();
    }
}