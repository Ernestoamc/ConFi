package com.confi.adapter.in.web;

import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.in.BudgetUseCases;
import com.confi.domain.port.in.BudgetUseCases.BudgetVsActualReport;
import com.confi.domain.port.in.BudgetUseCases.CategoryBudgetDelta;
import com.confi.domain.port.in.BudgetUseCases.PeriodScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
class ReportControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialReportUseCase financialReportUseCase;

        @MockitoBean
        private BudgetUseCases budgetUseCases;

    @Test
    void generaEstadoResultadosGeneralConEfectivoInformativo() throws Exception {
        when(financialReportUseCase.incomeStatement(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")),
                eq(null),
                eq(true)
        )).thenReturn(new FinancialReportUseCase.IncomeStatement(
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z"),
                null,
                true,
                new BigDecimal("10000.00"),
                new BigDecimal("7300.00"),
                new BigDecimal("2700.00")
        ));

        mockMvc.perform(get("/api/reports/income-statement")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z")
                        .param("includeInformativeCash", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.includeInformativeCash").value(true))
                .andExpect(jsonPath("$.resultadoNeto").value(2700.00));
    }

    @Test
    void generaEstadoResultadosPorCuenta() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        when(financialReportUseCase.incomeStatement(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")),
                eq(cuentaId),
                eq(false)
        )).thenReturn(new FinancialReportUseCase.IncomeStatement(
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z"),
                cuentaId,
                false,
                new BigDecimal("5000.00"),
                new BigDecimal("4000.00"),
                new BigDecimal("1000.00")
        ));

        mockMvc.perform(get("/api/reports/income-statement")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z")
                        .param("cuentaId", cuentaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value(cuentaId.toString()))
                .andExpect(jsonPath("$.totalIngresos").value(5000.00));
    }

    @Test
    void generaPresupuestoVsRealDesdeReports() throws Exception {
        BudgetVsActualReport report = new BudgetVsActualReport(
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z"),
                PeriodScope.MENSUAL,
                new BigDecimal("8000.00"),
                new BigDecimal("7600.00"),
                new BigDecimal("400.00"),
                List.of(new CategoryBudgetDelta(UUID.randomUUID(), new BigDecimal("2000.00"), new BigDecimal("1800.00")))
        );

        when(budgetUseCases.presupuestoVsReal(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")),
                eq(PeriodScope.MENSUAL)
        )).thenReturn(report);

        mockMvc.perform(get("/api/reports/budget-vs-actual")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z")
                        .param("scope", "MENSUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("MENSUAL"))
                .andExpect(jsonPath("$.diferencia").value(400.00));
    }
}
