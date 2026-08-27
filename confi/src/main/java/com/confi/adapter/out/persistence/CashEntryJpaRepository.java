package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.CashEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CashEntryJpaRepository extends JpaRepository<CashEntryEntity, UUID> {

    List<CashEntryEntity> findByFechaBetween(Instant desde, Instant hasta);
}
