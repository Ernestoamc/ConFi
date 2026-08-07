package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.SubscriptionChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubscriptionChargeJpaRepository extends JpaRepository<SubscriptionChargeEntity, UUID> {

    List<SubscriptionChargeEntity> findByFechaEsperadaBetween(LocalDate desde, LocalDate hasta);

    boolean existsBySubscripcionIdAndFechaEsperadaBetween(UUID subscripcionId, LocalDate desde, LocalDate hasta);
}