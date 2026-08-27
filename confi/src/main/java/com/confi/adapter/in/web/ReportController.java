package com.confi.adapter.in.web;

import com.confi.domain.port.in.FinancialReportUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final FinancialReportUseCase financialReportUseCase;

    public ReportController(FinancialReportUseCase financialReportUseCase) {
        this.financialReportUseCase = financialReportUseCase;
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

    public record IncomeStatementResponse(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            boolean includeInformativeCash,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto
    ) {}
}
