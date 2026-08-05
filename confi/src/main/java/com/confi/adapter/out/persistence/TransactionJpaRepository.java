package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByFechaBetween(Instant desde, Instant hasta);

    List<TransactionEntity> findByCategoriaIdAndFechaBetween(UUID categoriaId, Instant desde, Instant hasta);
}