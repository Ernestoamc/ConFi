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
    private final PeriodCloseService periodCloseService;

    public TransactionMaintenanceService(TransactionRepository transactionRepository,
                                         RegisterTransactionUseCase registerTransactionUseCase,
                                         TransactionReversalRepository reversalRepository) {
        this(transactionRepository, registerTransactionUseCase, reversalRepository, null);
    }

    public TransactionMaintenanceService(TransactionRepository transactionRepository,
                                         RegisterTransactionUseCase registerTransactionUseCase,
                                         TransactionReversalRepository reversalRepository,
                                         PeriodCloseService periodCloseService) {
        this.transactionRepository = transactionRepository;
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.reversalRepository = reversalRepository;
        this.periodCloseService = periodCloseService;
    }

    @Override
    @Transactional
    public Transaction actualizarNota(UUID transactionId, String nota) {
        Transaction tx = buscar(transactionId);
        validarPeriodoAbierto(tx, "actualizacion de nota");
        tx.actualizarNota(nota);
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public Transaction cancelar(UUID transactionId, String motivo, UUID categoriaReversaId) {
        Transaction original = buscar(transactionId);
        validarPeriodoAbierto(original, "cancelacion de transacciones");
        return cancelarInterna(original, motivo, categoriaReversaId);
    }

    @Override
    @Transactional
    public Transaction reemplazar(UUID transactionId, RegisterTransactionCommand nuevoMovimiento, String motivo) {
        if (nuevoMovimiento == null) {
            throw new IllegalArgumentException("El movimiento de reemplazo es obligatorio");
        }

        Transaction original = buscar(transactionId);
        validarPeriodoAbierto(original, "reemplazo de transacciones");

        UUID categoriaReversaId = (original.getTipo() == TransactionType.TRANSFERENCIA
                && original.getCuentaDestinoId() == null)
                ? nuevoMovimiento.categoriaId()
                : null;

        cancelarInterna(original,
                (motivo == null || motivo.isBlank()) ? "Reemplazo seguro" : motivo,
                categoriaReversaId);

        return registerTransactionUseCase.execute(new RegisterTransactionCommand(
                nuevoMovimiento.tipo(),
                nuevoMovimiento.monto(),
                nuevoMovimiento.nota(),
                nuevoMovimiento.cuentaOrigenId(),
                nuevoMovimiento.cuentaDestinoId(),
                nuevoMovimiento.categoriaId(),
                nuevoMovimiento.contraparte(),
                nuevoMovimiento.subscripcionId(),
                nuevoMovimiento.fecha()
        ));
    }

    private Transaction cancelarInterna(Transaction original, String motivo, UUID categoriaReversaId) {
        UUID transactionId = original.getId();

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

                if (categoriaReversaId == null) {
                    throw new IllegalArgumentException(
                            "Para cancelar transferencia a terceros debes enviar categoriaReversaId");
                }

                yield registerTransactionUseCase.execute(new RegisterTransactionCommand(
                        TransactionType.INGRESO,
                        original.getMonto(),
                        notaReversa,
                        original.getCuentaOrigenId(),
                        null,
                        categoriaReversaId,
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

    private void validarPeriodoAbierto(Transaction tx, String context) {
        if (periodCloseService == null) {
            return;
        }
        periodCloseService.ensureOpen(tx.getFecha(), context);
    }
}
