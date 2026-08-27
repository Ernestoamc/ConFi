package com.confi.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TransactionReversal {

    private final UUID id;
    private final UUID originalTransactionId;
    private final UUID reversalTransactionId;
    private final Instant createdAt;

    public TransactionReversal(UUID id, UUID originalTransactionId, UUID reversalTransactionId, Instant createdAt) {
        if (originalTransactionId == null || reversalTransactionId == null) {
            throw new IllegalArgumentException("Las transacciones de reversa son obligatorias");
        }
        if (originalTransactionId.equals(reversalTransactionId)) {
            throw new IllegalArgumentException("La reversa no puede apuntar a la misma transacción");
        }
        this.id = id;
        this.originalTransactionId = originalTransactionId;
        this.reversalTransactionId = reversalTransactionId;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static TransactionReversal create(UUID originalTransactionId, UUID reversalTransactionId) {
        return new TransactionReversal(UUID.randomUUID(), originalTransactionId, reversalTransactionId, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getOriginalTransactionId() { return originalTransactionId; }
    public UUID getReversalTransactionId() { return reversalTransactionId; }
    public Instant getCreatedAt() { return createdAt; }
}
