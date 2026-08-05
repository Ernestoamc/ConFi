package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.TransactionRepository;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Implementación del caso de uso central: registrar un movimiento y
 * actualizar los saldos de las cuentas involucradas de forma consistente.
 *
 * Nótese que esta clase NO importa nada de Spring: es Java puro.
 * @Transactional se aplica en el adapter/config, no aquí.
 */
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
                    command.cuentaOrigenId(), command.categoriaId(), fecha);
            case INGRESO -> Transaction.ingreso(command.monto(), command.nota(),
                    command.cuentaOrigenId(), command.categoriaId(), fecha);
            case TRANSFERENCIA -> command.cuentaDestinoId() != null
                    ? Transaction.transferenciaEntreCuentasPropias(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.cuentaDestinoId(), fecha)
                    : Transaction.transferenciaATercero(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.contraparte(), fecha);
        };

        aplicarEfectosEnCuentas(transaction);

        return transactionRepository.save(transaction);
    }

    private void aplicarEfectosEnCuentas(Transaction transaction) {
        Account cuentaOrigen = buscarCuenta(transaction.getCuentaOrigenId());

        switch (transaction.getTipo()) {
            case GASTO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);
            }
            case INGRESO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto());
                accountRepository.save(cuentaOrigen);
            }
            case TRANSFERENCIA -> {
                // Sale dinero de la cuenta origen en ambos casos (interna o a tercero)
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);

                if (transaction.esTransferenciaInterna()) {
                    Account cuentaDestino = buscarCuenta(transaction.getCuentaDestinoId());
                    cuentaDestino.aplicarMovimiento(transaction.getMonto());
                    accountRepository.save(cuentaDestino);
                }
                // Si es a un tercero, el dinero simplemente sale del sistema: no hay cuenta destino que actualizar.
            }
        }
    }

    private Account buscarCuenta(java.util.UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada: " + id));
    }
}