package com.confi.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FinancialReportUseCase {

    IncomeStatement incomeStatement(Instant desde, Instant hasta, UUID cuentaId, boolean includeInformativeCash);
    InsightsReport insights(Instant desde, Instant hasta, UUID cuentaId, int top);

    record IncomeStatement(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            boolean includeInformativeCash,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto
    ) {}

        record InsightsReport(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto,
            BigDecimal tasaAhorro,
            List<CategorySpendInsight> topCategoriasGasto,
            List<String> recomendaciones
        ) {}

        record CategorySpendInsight(
            UUID categoriaId,
            BigDecimal gastoTotal,
            BigDecimal porcentajeDelGasto
        ) {}
}
