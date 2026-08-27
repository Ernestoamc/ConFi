package com.confi.domain.port.out;

import com.confi.domain.model.TransactionReversal;

import java.util.UUID;

public interface TransactionReversalRepository {

    TransactionReversal save(TransactionReversal reversal);

    boolean existsByOriginalTransactionId(UUID originalTransactionId);

    boolean existsByReversalTransactionId(UUID reversalTransactionId);
}
