package com.confi.domain.service;

import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.out.CashEntryRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional(readOnly = true)
    public InsightsReport insights(Instant desde, Instant hasta, UUID cuentaId, int top) {
        validarPeriodo(desde, hasta);
        int safeTop = Math.max(1, Math.min(top, 20));

        List<Transaction> movimientos = cuentaId == null
                ? transactionRepository.findByPeriodo(desde, hasta)
                : transactionRepository.findByCuentaAndPeriodo(cuentaId, desde, hasta);

        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal gastos = BigDecimal.ZERO;
        Map<UUID, BigDecimal> gastoPorCategoria = new LinkedHashMap<>();

        for (Transaction tx : movimientos) {
            if (tx.getTipo() == TransactionType.INGRESO) {
                ingresos = ingresos.add(tx.getMonto());
            } else if (tx.getTipo() == TransactionType.GASTO) {
                gastos = gastos.add(tx.getMonto());
                if (tx.getCategoriaId() != null) {
                    gastoPorCategoria.merge(tx.getCategoriaId(), tx.getMonto(), BigDecimal::add);
                }
            }
        }

        BigDecimal resultado = ingresos.subtract(gastos);
        BigDecimal tasaAhorro = ingresos.compareTo(BigDecimal.ZERO) > 0
                ? resultado.divide(ingresos, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        final BigDecimal totalGastos = gastos;

        List<CategorySpendInsight> topCategorias = gastoPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(safeTop)
                .map(entry -> new CategorySpendInsight(
                        entry.getKey(),
                        entry.getValue(),
                totalGastos.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalGastos, 4, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO
                ))
                .toList();

        List<String> recomendaciones = construirRecomendaciones(ingresos, gastos, resultado, tasaAhorro, topCategorias);

        return new InsightsReport(
                desde,
                hasta,
                cuentaId,
                ingresos,
                gastos,
                resultado,
                tasaAhorro,
                topCategorias,
                recomendaciones
        );
    }

    private List<String> construirRecomendaciones(BigDecimal ingresos,
                                                  BigDecimal gastos,
                                                  BigDecimal resultado,
                                                  BigDecimal tasaAhorro,
                                                  List<CategorySpendInsight> topCategorias) {
        List<String> recomendaciones = new ArrayList<>();

        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            recomendaciones.add("Tus gastos superan tus ingresos en el periodo; prioriza recortes en categorias no esenciales.");
        }

        if (ingresos.compareTo(BigDecimal.ZERO) > 0 && tasaAhorro.compareTo(new BigDecimal("0.10")) < 0) {
            recomendaciones.add("Tu tasa de ahorro esta por debajo de 10%; intenta separar ahorro automatico al inicio del periodo.");
        }

        if (!topCategorias.isEmpty() && topCategorias.get(0).porcentajeDelGasto().compareTo(new BigDecimal("0.40")) > 0) {
            recomendaciones.add("Una sola categoria concentra mas del 40% del gasto; revisa suscripciones y gastos recurrentes en esa categoria.");
        }

        if (recomendaciones.isEmpty()) {
            recomendaciones.add("Tu patron de gasto luce estable; manten seguimiento semanal para sostener la tendencia.");
        }

        return recomendaciones;
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
