package com.confi.domain.service;

import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.out.CashEntryRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FinancialReportService implements FinancialReportUseCase {

    private final TransactionRepository transactionRepository;
    private final CashEntryRepository cashEntryRepository;

    public FinancialReportService(TransactionRepository transactionRepository,
                                  CashEntryRepository cashEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.cashEntryRepository = cashEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeStatement incomeStatement(Instant desde, Instant hasta, UUID cuentaId, boolean includeInformativeCash) {
        validarPeriodo(desde, hasta);

        List<Transaction> movimientos = cuentaId == null
                ? transactionRepository.findByPeriodo(desde, hasta)
                : transactionRepository.findByCuentaAndPeriodo(cuentaId, desde, hasta);

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;

        for (Transaction tx : movimientos) {
            if (tx.getTipo() == TransactionType.INGRESO) {
                ingresos = ingresos.add(tx.getMonto());
            } else if (tx.getTipo() == TransactionType.GASTO) {
                gastos = gastos.add(tx.getMonto());
            }
        }

        if (includeInformativeCash && cuentaId == null) {
            List<CashEntry> cashEntries = cashEntryRepository.findByPeriodo(desde, hasta);
            for (CashEntry cashEntry : cashEntries) {
                if (cashEntry.getMovimiento() == CashEntry.Movimiento.ABONO) {
                    ingresos = ingresos.add(cashEntry.getMonto());
                } else {
                    gastos = gastos.add(cashEntry.getMonto());
                }
            }
        }

        return new IncomeStatement(
                desde,
                hasta,
                cuentaId,
                includeInformativeCash,
                ingresos,
                gastos,
                ingresos.subtract(gastos)
        );
    }

    private void validarPeriodo(Instant desde, Instant hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parametros 'desde' y 'hasta' son obligatorios");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El parametro 'desde' no puede ser mayor que 'hasta'");
        }
    }
}
