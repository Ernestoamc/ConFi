package com.confi.domain.port.out;

import com.confi.domain.model.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByPeriodo(Instant desde, Instant hasta);

    List<Transaction> findByCategoriaAndPeriodo(UUID categoriaId, Instant desde, Instant hasta);
}