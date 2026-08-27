package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class TransactionQueryService implements TransactionQueryUseCase {

    private static final Comparator<Transaction> BY_FECHA_Y_ID =
            Comparator.comparing(Transaction::getFecha).thenComparing(Transaction::getId);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionQueryService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listar(Instant desde, Instant hasta, UUID cuentaId) {
        validarPeriodo(desde, hasta);
        List<Transaction> movimientos = cuentaId == null
                ? transactionRepository.findByPeriodo(desde, hasta)
                : transactionRepository.findByCuentaAndPeriodo(cuentaId, desde, hasta);
        return movimientos.stream().sorted(BY_FECHA_Y_ID).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatement estadoCuenta(UUID cuentaId, Instant desde, Instant hasta) {
        validarPeriodo(desde, hasta);
        Account cuenta = accountRepository.findById(cuentaId)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada: " + cuentaId));

        List<Transaction> movimientos = transactionRepository.findByCuentaAndPeriodo(cuentaId, desde, hasta)
                .stream().sorted(BY_FECHA_Y_ID).toList();

        if (movimientos.isEmpty()) {
            return new AccountStatement(cuentaId, cuenta.getSaldo(), cuenta.getSaldo(), List.of());
        }

        List<StatementEntry> entries = new ArrayList<>();
        BigDecimal saldoInicial = null;
        BigDecimal saldoFinal = null;

        for (Transaction tx : movimientos) {
            SignedMovement signed = signedMovementForAccount(tx, cuentaId);
            if (saldoInicial == null) {
                saldoInicial = signed.saldoDespues().subtract(signed.signedAmount());
            }
            saldoFinal = signed.saldoDespues();

            entries.add(new StatementEntry(
                    tx.getId(),
                    tx.getFecha(),
                    tx.getNota(),
                    tx.getTipo().name(),
                    signed.movimiento(),
                    tx.getMonto(),
                    signed.saldoDespues(),
                    cuentaId
            ));
        }

        return new AccountStatement(cuentaId, saldoInicial, saldoFinal, entries);
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralStatement estadoGeneral(Instant desde, Instant hasta) {
        validarPeriodo(desde, hasta);
        List<Transaction> movimientos = transactionRepository.findByPeriodo(desde, hasta)
                .stream().sorted(BY_FECHA_Y_ID).toList();

        if (movimientos.isEmpty()) {
            return new GeneralStatement(BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        }

        Map<UUID, List<Transaction>> porCuenta = new LinkedHashMap<>();
        for (Transaction tx : movimientos) {
            porCuenta.computeIfAbsent(tx.getCuentaOrigenId(), ignored -> new ArrayList<>()).add(tx);
            if (tx.getCuentaDestinoId() != null) {
                porCuenta.computeIfAbsent(tx.getCuentaDestinoId(), ignored -> new ArrayList<>()).add(tx);
            }
        }

        List<AccountStatement> porCuentaStatements = new ArrayList<>();
        BigDecimal saldoInicial = BigDecimal.ZERO;
        BigDecimal saldoFinal = BigDecimal.ZERO;

        for (UUID cuentaId : porCuenta.keySet()) {
            AccountStatement statement = buildAccountStatement(cuentaId, porCuenta.get(cuentaId));
            porCuentaStatements.add(statement);
            saldoInicial = saldoInicial.add(statement.saldoInicial());
            saldoFinal = saldoFinal.add(statement.saldoFinal());
        }

        return new GeneralStatement(saldoInicial, saldoFinal, porCuentaStatements);
    }

    private AccountStatement buildAccountStatement(UUID cuentaId, List<Transaction> movimientos) {
        List<Transaction> ordenados = movimientos.stream().sorted(BY_FECHA_Y_ID).toList();
        List<StatementEntry> entries = new ArrayList<>();

        BigDecimal saldoInicial = null;
        BigDecimal saldoFinal = null;

        for (Transaction tx : ordenados) {
            SignedMovement signed = signedMovementForAccount(tx, cuentaId);
            if (saldoInicial == null) {
                saldoInicial = signed.saldoDespues().subtract(signed.signedAmount());
            }
            saldoFinal = signed.saldoDespues();

            entries.add(new StatementEntry(
                    tx.getId(),
                    tx.getFecha(),
                    tx.getNota(),
                    tx.getTipo().name(),
                    signed.movimiento(),
                    tx.getMonto(),
                    signed.saldoDespues(),
                    cuentaId
            ));
        }

        return new AccountStatement(cuentaId, saldoInicial, saldoFinal, entries);
    }

    private SignedMovement signedMovementForAccount(Transaction tx, UUID cuentaId) {
        if (tx.getCuentaOrigenId().equals(cuentaId)) {
            BigDecimal signedAmount = switch (tx.getTipo()) {
                case INGRESO -> tx.getMonto();
                case GASTO, TRANSFERENCIA -> tx.getMonto().negate();
            };
            Movimiento movimiento = signedAmount.signum() >= 0 ? Movimiento.ABONO : Movimiento.CARGO;
            return new SignedMovement(signedAmount, movimiento, tx.getSaldoOrigenDespues());
        }

        if (tx.getCuentaDestinoId() != null && tx.getCuentaDestinoId().equals(cuentaId)) {
            return new SignedMovement(tx.getMonto(), Movimiento.ABONO, tx.getSaldoDestinoDespues());
        }

        throw new IllegalArgumentException("La transaccion no pertenece a la cuenta solicitada: " + cuentaId);
    }

    private void validarPeriodo(Instant desde, Instant hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parametros 'desde' y 'hasta' son obligatorios");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El parametro 'desde' no puede ser mayor que 'hasta'");
        }
    }

    private record SignedMovement(BigDecimal signedAmount, Movimiento movimiento, BigDecimal saldoDespues) {}
}
