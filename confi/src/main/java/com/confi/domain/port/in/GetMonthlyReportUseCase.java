package com.confi.domain.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GetMonthlyReportUseCase {

    MonthlyReport execute(int mes, int anio);

    record CategoryBreakdown(UUID categoriaId, String categoriaNombre,
                              BigDecimal planeado, BigDecimal real) {}

    record MonthlyReport(int mes, int anio, BigDecimal totalIngresos, BigDecimal totalGastos,
                          List<CategoryBreakdown> porCategoria) {}
}