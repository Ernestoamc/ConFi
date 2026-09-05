package com.confi.domain.service;

import com.confi.domain.model.Budget;
import com.confi.domain.model.PeriodicBudget;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.BudgetUseCases;
import com.confi.domain.port.out.BudgetRepository;
import com.confi.domain.port.out.PeriodicBudgetRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class BudgetService implements BudgetUseCases {

    private final BudgetRepository budgetRepository;
    private final PeriodicBudgetRepository periodicBudgetRepository;
    private final TransactionRepository transactionRepository;
    private final PeriodCloseService periodCloseService;

    public BudgetService(BudgetRepository budgetRepository,
                         PeriodicBudgetRepository periodicBudgetRepository,
                         TransactionRepository transactionRepository) {
        this(budgetRepository, periodicBudgetRepository, transactionRepository, null);
    }

    public BudgetService(BudgetRepository budgetRepository,
                         PeriodicBudgetRepository periodicBudgetRepository,
                         TransactionRepository transactionRepository,
                         PeriodCloseService periodCloseService) {
        this.budgetRepository = budgetRepository;
        this.periodicBudgetRepository = periodicBudgetRepository;
        this.transactionRepository = transactionRepository;
        this.periodCloseService = periodCloseService;
    }

    @Override
    @Transactional
    public Budget crearMensual(int mes, int anio, UUID categoriaId, BigDecimal montoPlaneado) {
        validarPeriodoAbierto(anio, mes, "creacion de presupuesto mensual");
        budgetRepository.findByCategoriaMesAnio(categoriaId, mes, anio)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe presupuesto mensual para la categoria y periodo");
                });
        return budgetRepository.save(Budget.crearNuevo(mes, anio, categoriaId, montoPlaneado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> listarMensual(int mes, int anio) {
        return budgetRepository.findByMesAndAnio(mes, anio);
    }

    @Override
    @Transactional
    public Budget ajustarMensual(UUID budgetId, BigDecimal nuevoMonto) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NoSuchElementException("Presupuesto no encontrado: " + budgetId));
        validarPeriodoAbierto(budget.getAnio(), budget.getMes(), "ajuste de presupuesto mensual");
        budget.ajustarMonto(nuevoMonto);
        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public PeriodicBudget crearPeriodico(PeriodicBudget.PeriodType periodType,
                                         LocalDate desde,
                                         LocalDate hasta,
                                         UUID categoriaId,
                                         BigDecimal montoPlaneado) {
        validarLongitudPeriodo(periodType, desde, hasta);
        validarPeriodoAbierto(desde, "creacion de presupuesto periodico");
        validarPeriodoAbierto(hasta, "creacion de presupuesto periodico");
        return periodicBudgetRepository.save(
                PeriodicBudget.crearNuevo(periodType, desde, hasta, categoriaId, montoPlaneado)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodicBudget> listarPeriodico(PeriodicBudget.PeriodType periodType, LocalDate desde, LocalDate hasta) {
        return periodicBudgetRepository.findByPeriodTypeAndRange(periodType, desde, hasta);
    }

    @Override
    @Transactional
    public PeriodicBudget ajustarPeriodico(UUID budgetId, BigDecimal nuevoMonto) {
        PeriodicBudget budget = periodicBudgetRepository.findById(budgetId)
                .orElseThrow(() -> new NoSuchElementException("Presupuesto periodico no encontrado: " + budgetId));
        validarPeriodoAbierto(budget.getDesde(), "ajuste de presupuesto periodico");
        validarPeriodoAbierto(budget.getHasta(), "ajuste de presupuesto periodico");
        budget.ajustarMonto(nuevoMonto);
        return periodicBudgetRepository.save(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetVsActualReport presupuestoVsReal(Instant desde, Instant hasta, PeriodScope scope) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parametros 'desde' y 'hasta' son obligatorios");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El parametro 'desde' no puede ser mayor que 'hasta'");
        }

        PeriodScope resolvedScope = scope == null ? PeriodScope.TODOS : scope;

        Map<UUID, BigDecimal> planeadoPorCategoria = calcularPlaneado(desde, hasta, resolvedScope);
        Map<UUID, BigDecimal> realPorCategoria = calcularRealGasto(desde, hasta);

        Map<UUID, CategoryBudgetDelta> deltas = new LinkedHashMap<>();
        BigDecimal totalPlaneado = BigDecimal.ZERO;
        BigDecimal totalReal = BigDecimal.ZERO;

        for (UUID categoriaId : planeadoPorCategoria.keySet()) {
            BigDecimal planeado = planeadoPorCategoria.getOrDefault(categoriaId, BigDecimal.ZERO);
            BigDecimal real = realPorCategoria.getOrDefault(categoriaId, BigDecimal.ZERO);
            deltas.put(categoriaId, new CategoryBudgetDelta(categoriaId, planeado, real));
            totalPlaneado = totalPlaneado.add(planeado);
            totalReal = totalReal.add(real);
        }

        for (UUID categoriaId : realPorCategoria.keySet()) {
            if (deltas.containsKey(categoriaId)) {
                continue;
            }
            BigDecimal real = realPorCategoria.get(categoriaId);
            deltas.put(categoriaId, new CategoryBudgetDelta(categoriaId, BigDecimal.ZERO, real));
            totalReal = totalReal.add(real);
        }

        return new BudgetVsActualReport(
                desde,
                hasta,
                resolvedScope,
                totalPlaneado,
                totalReal,
                totalPlaneado.subtract(totalReal),
                new ArrayList<>(deltas.values())
        );
    }

    private Map<UUID, BigDecimal> calcularPlaneado(Instant desde, Instant hasta, PeriodScope scope) {
        Map<UUID, BigDecimal> resultado = new LinkedHashMap<>();
        LocalDate from = desde.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate to = hasta.atZone(ZoneOffset.UTC).toLocalDate();

        if (scope == PeriodScope.TODOS || scope == PeriodScope.MENSUAL) {
            YearMonth cursor = YearMonth.from(from);
            YearMonth end = YearMonth.from(to);
            while (!cursor.isAfter(end)) {
                for (Budget budget : budgetRepository.findByMesAndAnio(cursor.getMonthValue(), cursor.getYear())) {
                    resultado.merge(budget.getCategoriaId(), budget.getMontoPlaneado(), BigDecimal::add);
                }
                cursor = cursor.plusMonths(1);
            }
        }

        if (scope == PeriodScope.TODOS || scope == PeriodScope.SEMANAL) {
            for (PeriodicBudget budget : periodicBudgetRepository.findByPeriodTypeAndRange(
                    PeriodicBudget.PeriodType.SEMANAL, from, to)) {
                resultado.merge(budget.getCategoriaId(), budget.getMontoPlaneado(), BigDecimal::add);
            }
        }

        if (scope == PeriodScope.TODOS || scope == PeriodScope.QUINCENAL) {
            for (PeriodicBudget budget : periodicBudgetRepository.findByPeriodTypeAndRange(
                    PeriodicBudget.PeriodType.QUINCENAL, from, to)) {
                resultado.merge(budget.getCategoriaId(), budget.getMontoPlaneado(), BigDecimal::add);
            }
        }

        return resultado;
    }

    private Map<UUID, BigDecimal> calcularRealGasto(Instant desde, Instant hasta) {
        Map<UUID, BigDecimal> resultado = new LinkedHashMap<>();
        for (Transaction tx : transactionRepository.findByPeriodo(desde, hasta)) {
            if (tx.getTipo() != TransactionType.GASTO || tx.getCategoriaId() == null) {
                continue;
            }
            resultado.merge(tx.getCategoriaId(), tx.getMonto(), BigDecimal::add);
        }
        return resultado;
    }

    private void validarLongitudPeriodo(PeriodicBudget.PeriodType periodType, LocalDate desde, LocalDate hasta) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (periodType == PeriodicBudget.PeriodType.SEMANAL && days > 7) {
            throw new IllegalArgumentException("Un presupuesto semanal no debe exceder 7 dias");
        }
        if (periodType == PeriodicBudget.PeriodType.QUINCENAL && days > 16) {
            throw new IllegalArgumentException("Un presupuesto quincenal no debe exceder 16 dias");
        }
    }

    private void validarPeriodoAbierto(int anio, int mes, String context) {
        if (periodCloseService == null) {
            return;
        }
        Instant instant = YearMonth.of(anio, mes).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        periodCloseService.ensureOpen(instant, context);
    }

    private void validarPeriodoAbierto(LocalDate date, String context) {
        if (periodCloseService == null) {
            return;
        }
        periodCloseService.ensureOpen(date.atStartOfDay().toInstant(ZoneOffset.UTC), context);
    }
}
