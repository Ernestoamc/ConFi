package com.confi.adapter.in.web;

import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.in.BudgetUseCases;
import com.confi.domain.port.in.BudgetUseCases.BudgetVsActualReport;
import com.confi.domain.port.in.BudgetUseCases.PeriodScope;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final FinancialReportUseCase financialReportUseCase;
        private final BudgetUseCases budgetUseCases;

        public ReportController(FinancialReportUseCase financialReportUseCase,
                                                        BudgetUseCases budgetUseCases) {
        this.financialReportUseCase = financialReportUseCase;
                this.budgetUseCases = budgetUseCases;
    }

    @GetMapping("/income-statement")
    public IncomeStatementResponse incomeStatement(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) UUID cuentaId,
            @RequestParam(defaultValue = "false") boolean includeInformativeCash) {
        FinancialReportUseCase.IncomeStatement statement =
                financialReportUseCase.incomeStatement(desde, hasta, cuentaId, includeInformativeCash);
        return new IncomeStatementResponse(
                statement.desde(),
                statement.hasta(),
                statement.cuentaId(),
                statement.includeInformativeCash(),
                statement.totalIngresos(),
                statement.totalGastos(),
                statement.resultadoNeto()
        );
    }

        @GetMapping("/budget-vs-actual")
        public BudgetVsActualResponse budgetVsActual(
                        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
                        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
                        @RequestParam(defaultValue = "TODOS") PeriodScope scope) {
                BudgetVsActualReport report = budgetUseCases.presupuestoVsReal(desde, hasta, scope);
                return new BudgetVsActualResponse(
                                report.desde(),
                                report.hasta(),
                                report.scope(),
                                report.totalPlaneado(),
                                report.totalReal(),
                                report.diferencia(),
                                report.porCategoria().stream()
                                                .map(delta -> new CategoryBudgetDeltaResponse(delta.categoriaId(), delta.planeado(), delta.real()))
                                                .toList()
                );
        }

    public record IncomeStatementResponse(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            boolean includeInformativeCash,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto
    ) {}

    public record CategoryBudgetDeltaResponse(UUID categoriaId, BigDecimal planeado, BigDecimal real) {}

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
