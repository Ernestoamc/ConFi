package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.TransactionReversalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionReversalJpaRepository extends JpaRepository<TransactionReversalEntity, UUID> {

    boolean existsByOriginalTransactionId(UUID originalTransactionId);

    boolean existsByReversalTransactionId(UUID reversalTransactionId);
}
