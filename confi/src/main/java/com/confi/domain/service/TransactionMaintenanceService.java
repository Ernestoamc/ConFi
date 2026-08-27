package com.confi.domain.service;

import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.model.TransactionReversal;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.in.TransactionMaintenanceUseCase;
import com.confi.domain.port.out.TransactionRepository;
import com.confi.domain.port.out.TransactionReversalRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

public class TransactionMaintenanceService implements TransactionMaintenanceUseCase {

    private final TransactionRepository transactionRepository;
    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final TransactionReversalRepository reversalRepository;

    public TransactionMaintenanceService(TransactionRepository transactionRepository,
                                         RegisterTransactionUseCase registerTransactionUseCase,
                                         TransactionReversalRepository reversalRepository) {
        this.transactionRepository = transactionRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.reversalRepository = reversalRepository;
    }

    @Override
    @Transactional
    public Transaction actualizarNota(UUID transactionId, String nota) {
        Transaction tx = buscar(transactionId);
        tx.actualizarNota(nota);
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public Transaction cancelar(UUID transactionId, String motivo) {
        Transaction original = buscar(transactionId);

        if (reversalRepository.existsByOriginalTransactionId(transactionId)
                || reversalRepository.existsByReversalTransactionId(transactionId)) {
            throw new IllegalArgumentException("La transacción ya fue cancelada o es una reversa");
        }

        String notaReversa = (motivo == null || motivo.isBlank())
                ? "Reversa de " + transactionId
                : "Reversa de " + transactionId + ": " + motivo.trim();

        Transaction reversa = switch (original.getTipo()) {
            case GASTO -> registerTransactionUseCase.execute(new RegisterTransactionCommand(
                    TransactionType.INGRESO,
                    original.getMonto(),
                    notaReversa,
                    original.getCuentaOrigenId(),
                    null,
                    original.getCategoriaId(),
                    original.getContraparte(),
                    original.getSubscripcionId(),
                    Instant.now()
            ));
            case INGRESO -> registerTransactionUseCase.execute(new RegisterTransactionCommand(
                    TransactionType.GASTO,
                    original.getMonto(),
                    notaReversa,
                    original.getCuentaOrigenId(),
                    null,
                    original.getCategoriaId(),
                    original.getContraparte(),
                    original.getSubscripcionId(),
                    Instant.now()
            ));
            case TRANSFERENCIA -> {
                if (original.getCuentaDestinoId() != null) {
                    yield registerTransactionUseCase.execute(new RegisterTransactionCommand(
                            TransactionType.TRANSFERENCIA,
                            original.getMonto(),
                            notaReversa,
                            original.getCuentaDestinoId(),
                            original.getCuentaOrigenId(),
                            null,
                            null,
                            null,
                            Instant.now()
                    ));
                }
                yield registerTransactionUseCase.execute(new RegisterTransactionCommand(
                        TransactionType.INGRESO,
                        original.getMonto(),
                        notaReversa,
                        original.getCuentaOrigenId(),
                        null,
                        original.getCategoriaId(),
                        original.getContraparte(),
                        null,
                        Instant.now()
                ));
            }
        };

        reversalRepository.save(TransactionReversal.create(original.getId(), reversa.getId()));
        return reversa;
    }

    private Transaction buscar(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transacción no encontrada: " + id));
    }
}
