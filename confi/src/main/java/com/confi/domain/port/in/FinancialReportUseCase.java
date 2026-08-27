package com.confi.domain.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface FinancialReportUseCase {

    IncomeStatement incomeStatement(Instant desde, Instant hasta, UUID cuentaId, boolean includeInformativeCash);

    record IncomeStatement(
            Instant desde,
            Instant hasta,
            UUID cuentaId,
            boolean includeInformativeCash,
            BigDecimal totalIngresos,
            BigDecimal totalGastos,
            BigDecimal resultadoNeto
    ) {}
}
