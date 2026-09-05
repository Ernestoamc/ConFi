package com.confi.domain.port.in;

import com.confi.domain.model.Budget;
import com.confi.domain.model.PeriodicBudget;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BudgetUseCases {

    Budget crearMensual(int mes, int anio, UUID categoriaId, BigDecimal montoPlaneado);

    List<Budget> listarMensual(int mes, int anio);

    Budget ajustarMensual(UUID budgetId, BigDecimal nuevoMonto);

    PeriodicBudget crearPeriodico(PeriodicBudget.PeriodType periodType,
                                  LocalDate desde,
                                  LocalDate hasta,
                                  UUID categoriaId,
                                  BigDecimal montoPlaneado);

    List<PeriodicBudget> listarPeriodico(PeriodicBudget.PeriodType periodType, LocalDate desde, LocalDate hasta);

    PeriodicBudget ajustarPeriodico(UUID budgetId, BigDecimal nuevoMonto);

    BudgetVsActualReport presupuestoVsReal(Instant desde, Instant hasta, PeriodScope scope);

    enum PeriodScope { MENSUAL, SEMANAL, QUINCENAL, TODOS }

    record CategoryBudgetDelta(UUID categoriaId, BigDecimal planeado, BigDecimal real) {}

    record BudgetVsActualReport(
            Instant desde,
            Instant hasta,
            PeriodScope scope,
            BigDecimal totalPlaneado,
            BigDecimal totalReal,
            BigDecimal diferencia,
            List<CategoryBudgetDelta> porCategoria
    ) {}
}
