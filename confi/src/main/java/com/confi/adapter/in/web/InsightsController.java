package com.confi.adapter.in.web;

import com.confi.domain.port.in.FinancialReportUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/insights")
@Validated
public class InsightsController {

    private final FinancialReportUseCase financialReportUseCase;

    public InsightsController(FinancialReportUseCase financialReportUseCase) {
        this.financialReportUseCase = financialReportUseCase;
    }

    @GetMapping
    public InsightsResponse getInsights(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) UUID cuentaId,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) Integer top) {
        FinancialReportUseCase.InsightsReport report = financialReportUseCase.insights(desde, hasta, cuentaId, top);
        return new InsightsResponse(
                report.desde(),
                report.hasta(),
                report.cuentaId(),
                report.totalIngresos(),
                report.totalGastos(),
                report.resultadoNeto(),
                report.tasaAhorro(),
                report.topCategoriasGasto().stream()
                        .map(item -> new CategorySpendInsightResponse(
                                item.categoriaId(),
                                item.gastoTotal(),
                                item.porcentajeDelGasto()
                        ))
                        .toList(),
                report.recomendaciones()
        );
    }

    public record InsightsResponse(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto,
            BigDecimal tasaAhorro,
            List<CategorySpendInsightResponse> topCategoriasGasto,
            List<String> recomendaciones
    ) {
    }

    public record CategorySpendInsightResponse(
            UUID categoriaId,
            BigDecimal gastoTotal,
            BigDecimal porcentajeDelGasto
    ) {
    }
}
