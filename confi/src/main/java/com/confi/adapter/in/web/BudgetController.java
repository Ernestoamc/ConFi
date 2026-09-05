package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.BudgetDtos.BudgetVsActualResponse;
import com.confi.adapter.in.web.dto.BudgetDtos.CategoryBudgetDeltaResponse;
import com.confi.adapter.in.web.dto.BudgetDtos.CreateMonthlyBudgetRequest;
import com.confi.adapter.in.web.dto.BudgetDtos.CreatePeriodicBudgetRequest;
import com.confi.adapter.in.web.dto.BudgetDtos.MonthlyBudgetResponse;
import com.confi.adapter.in.web.dto.BudgetDtos.PeriodicBudgetResponse;
import com.confi.adapter.in.web.dto.BudgetDtos.UpdateBudgetAmountRequest;
import com.confi.domain.model.Budget;
import com.confi.domain.model.PeriodicBudget;
import com.confi.domain.port.in.BudgetUseCases;
import com.confi.domain.port.in.BudgetUseCases.BudgetVsActualReport;
import com.confi.domain.port.in.BudgetUseCases.PeriodScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@Validated
public class BudgetController {

    private final BudgetUseCases budgetUseCases;

    public BudgetController(BudgetUseCases budgetUseCases) {
        this.budgetUseCases = budgetUseCases;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonthlyBudgetResponse crearMensual(@Valid @RequestBody CreateMonthlyBudgetRequest request) {
        Budget created = budgetUseCases.crearMensual(
                request.mes(), request.anio(), request.categoriaId(), request.montoPlaneado());
        return toMonthlyResponse(created);
    }

    @GetMapping
    public List<MonthlyBudgetResponse> listarMensual(
            @RequestParam @Min(1) @Max(12) int mes,
            @RequestParam @Min(1900) int anio) {
        return budgetUseCases.listarMensual(mes, anio).stream().map(this::toMonthlyResponse).toList();
    }

    @PatchMapping("/{id}")
    public MonthlyBudgetResponse ajustarMensual(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateBudgetAmountRequest request) {
        return toMonthlyResponse(budgetUseCases.ajustarMensual(id, request.montoPlaneado()));
    }

    @PostMapping("/biweekly")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodicBudgetResponse crearQuincenal(@Valid @RequestBody CreatePeriodicBudgetRequest request) {
        PeriodicBudget created = budgetUseCases.crearPeriodico(
                PeriodicBudget.PeriodType.QUINCENAL,
                request.desde(),
                request.hasta(),
                request.categoriaId(),
                request.montoPlaneado());
        return toPeriodicResponse(created);
    }

    @GetMapping("/biweekly")
    public List<PeriodicBudgetResponse> listarQuincenal(
            @RequestParam @NotNull LocalDate desde,
            @RequestParam @NotNull LocalDate hasta) {
        return budgetUseCases.listarPeriodico(PeriodicBudget.PeriodType.QUINCENAL, desde, hasta)
                .stream().map(this::toPeriodicResponse).toList();
    }

    @PatchMapping("/biweekly/{id}")
    public PeriodicBudgetResponse ajustarQuincenal(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateBudgetAmountRequest request) {
        return toPeriodicResponse(budgetUseCases.ajustarPeriodico(id, request.montoPlaneado()));
    }

    @PostMapping("/weekly")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodicBudgetResponse crearSemanal(@Valid @RequestBody CreatePeriodicBudgetRequest request) {
        PeriodicBudget created = budgetUseCases.crearPeriodico(
                PeriodicBudget.PeriodType.SEMANAL,
                request.desde(),
                request.hasta(),
                request.categoriaId(),
                request.montoPlaneado());
        return toPeriodicResponse(created);
    }

    @GetMapping("/weekly")
    public List<PeriodicBudgetResponse> listarSemanal(
            @RequestParam @NotNull LocalDate desde,
            @RequestParam @NotNull LocalDate hasta) {
        return budgetUseCases.listarPeriodico(PeriodicBudget.PeriodType.SEMANAL, desde, hasta)
                .stream().map(this::toPeriodicResponse).toList();
    }

    @PatchMapping("/weekly/{id}")
    public PeriodicBudgetResponse ajustarSemanal(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateBudgetAmountRequest request) {
        return toPeriodicResponse(budgetUseCases.ajustarPeriodico(id, request.montoPlaneado()));
    }

    @GetMapping("/vs-actual")
    public BudgetVsActualResponse presupuestoVsReal(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(defaultValue = "TODOS") PeriodScope scope) {
        return toVsActualResponse(budgetUseCases.presupuestoVsReal(desde, hasta, scope));
    }

    private MonthlyBudgetResponse toMonthlyResponse(Budget budget) {
        return new MonthlyBudgetResponse(
                budget.getId(),
                budget.getMes(),
                budget.getAnio(),
                budget.getCategoriaId(),
                budget.getMontoPlaneado()
        );
    }

    private PeriodicBudgetResponse toPeriodicResponse(PeriodicBudget budget) {
        return new PeriodicBudgetResponse(
                budget.getId(),
                budget.getPeriodType(),
                budget.getDesde(),
                budget.getHasta(),
                budget.getCategoriaId(),
                budget.getMontoPlaneado()
        );
    }

    private BudgetVsActualResponse toVsActualResponse(BudgetVsActualReport report) {
        List<CategoryBudgetDeltaResponse> porCategoria = report.porCategoria().stream()
                .map(delta -> new CategoryBudgetDeltaResponse(delta.categoriaId(), delta.planeado(), delta.real()))
                .toList();
        return new BudgetVsActualResponse(
                report.desde(),
                report.hasta(),
                report.scope(),
                report.totalPlaneado(),
                report.totalReal(),
                report.diferencia(),
                porCategoria
        );
    }
}
