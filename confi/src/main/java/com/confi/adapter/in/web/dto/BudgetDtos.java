package com.confi.adapter.in.web.dto;

import com.confi.domain.model.PeriodicBudget;
import com.confi.domain.port.in.BudgetUseCases.PeriodScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BudgetDtos {

    public record CreateMonthlyBudgetRequest(
            @Min(1) @Max(12) int mes,
            @Min(1900) int anio,
            @NotNull UUID categoriaId,
            @NotNull @PositiveOrZero BigDecimal montoPlaneado
    ) {}

    public record UpdateBudgetAmountRequest(
            @NotNull @PositiveOrZero BigDecimal montoPlaneado
    ) {}

    public record CreatePeriodicBudgetRequest(
            @NotNull LocalDate desde,
            @NotNull LocalDate hasta,
            @NotNull UUID categoriaId,
            @NotNull @PositiveOrZero BigDecimal montoPlaneado
    ) {}

    public record MonthlyBudgetResponse(
            UUID id,
            int mes,
            int anio,
            UUID categoriaId,
            BigDecimal montoPlaneado
    ) {}

    public record PeriodicBudgetResponse(
            UUID id,
            PeriodicBudget.PeriodType periodType,
            LocalDate desde,
            LocalDate hasta,
            UUID categoriaId,
            BigDecimal montoPlaneado
    ) {}

    public record CategoryBudgetDeltaResponse(
            UUID categoriaId,
            BigDecimal planeado,
            BigDecimal real
    ) {}

    public record BudgetVsActualResponse(
            Instant desde,
            Instant hasta,
            PeriodScope scope,
            BigDecimal totalPlaneado,
            BigDecimal totalReal,
            BigDecimal diferencia,
            List<CategoryBudgetDeltaResponse> porCategoria
    ) {}
}
