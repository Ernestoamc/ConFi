package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.TransactionRepository;

import java.time.Instant;
import java.util.NoSuchElementException;

public class RegisterTransactionService implements RegisterTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public RegisterTransactionService(TransactionRepository transactionRepository,
                                       AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction execute(RegisterTransactionCommand command) {
        Instant fecha = command.fecha() != null ? command.fecha() : Instant.now();

        Transaction transaction = switch (command.tipo()) {
            case GASTO -> Transaction.gasto(command.monto(), command.nota(),
                    command.cuentaOrigenId(), command.categoriaId(), command.contraparte(), 
                    command.subscripcionId(), fecha);
            case INGRESO -> Transaction.ingreso(command.monto(), command.nota(),
                    command.cuentaOrigenId(), command.categoriaId(), command.contraparte(), fecha);
            case TRANSFERENCIA -> command.cuentaDestinoId() != null
                    ? Transaction.transferenciaEntreCuentasPropias(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.cuentaDestinoId(), fecha)
                    : Transaction.transferenciaATercero(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.contraparte(), fecha);
        };

        aplicarEfectosEnCuentasYRegistrarSaldos(transaction);

        return transactionRepository.save(transaction);
    }

    private void aplicarEfectosEnCuentasYRegistrarSaldos(Transaction transaction) {
        Account cuentaOrigen = buscarCuenta(transaction.getCuentaOrigenId());

        switch (transaction.getTipo()) {
            case GASTO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);
                transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
            }
            case INGRESO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto());
                accountRepository.save(cuentaOrigen);
                transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
            }
            case TRANSFERENCIA -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);

                if (transaction.esTransferenciaInterna()) {
                    Account cuentaDestino = buscarCuenta(transaction.getCuentaDestinoId());
                    cuentaDestino.aplicarMovimiento(transaction.getMonto());
                    accountRepository.save(cuentaDestino);
                    transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), cuentaDestino.getSaldo());
                } else {
                    transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
                }
            }
        }
    }

    private Account buscarCuenta(java.util.UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada: " + id));
    }
}