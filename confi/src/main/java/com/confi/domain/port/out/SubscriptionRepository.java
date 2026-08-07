package com.confi.domain.port.out;

import com.confi.domain.model.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);
    Optional<Subscription> findById(UUID id);
    List<Subscription> findAllActive();
    
} 
