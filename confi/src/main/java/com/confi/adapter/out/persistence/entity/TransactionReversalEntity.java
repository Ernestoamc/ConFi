package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_reversals")
public class TransactionReversalEntity {

    @Id
    private UUID id;

    @Column(name = "original_transaction_id", nullable = false, unique = true)
    private UUID originalTransactionId;

    @Column(name = "reversal_transaction_id", nullable = false, unique = true)
    private UUID reversalTransactionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionReversalEntity() {
    }

    public TransactionReversalEntity(UUID id, UUID originalTransactionId, UUID reversalTransactionId, Instant createdAt) {
        this.id = id;
        this.originalTransactionId = originalTransactionId;
        this.reversalTransactionId = reversalTransactionId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOriginalTransactionId() { return originalTransactionId; }
    public UUID getReversalTransactionId() { return reversalTransactionId; }
    public Instant getCreatedAt() { return createdAt; }
}
