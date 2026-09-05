package com.confi.adapter.in.web;

import com.confi.domain.model.Budget;
import com.confi.domain.model.PeriodicBudget;
import com.confi.domain.port.in.BudgetUseCases;
import com.confi.domain.port.in.BudgetUseCases.BudgetVsActualReport;
import com.confi.domain.port.in.BudgetUseCases.CategoryBudgetDelta;
import com.confi.domain.port.in.BudgetUseCases.PeriodScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BudgetController.class)
class BudgetControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetUseCases budgetUseCases;

    @Test
    void creaPresupuestoMensual() throws Exception {
        Budget budget = Budget.crearNuevo(8, 2026, UUID.randomUUID(), new BigDecimal("3000.00"));
        when(budgetUseCases.crearMensual(eq(8), eq(2026), any(UUID.class), eq(new BigDecimal("3000.00"))))
                .thenReturn(budget);

        String payload = """
                {
                  "mes": 8,
                  "anio": 2026,
                  "categoriaId": "%s",
                  "montoPlaneado": 3000.00
                }
                """.formatted(budget.getCategoriaId());

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mes").value(8))
                .andExpect(jsonPath("$.montoPlaneado").value(3000.00));
    }

    @Test
    void ajustaPresupuestoMensual() throws Exception {
        UUID id = UUID.randomUUID();
        Budget budget = new Budget(id, 8, 2026, UUID.randomUUID(), new BigDecimal("3500.00"));
        when(budgetUseCases.ajustarMensual(eq(id), eq(new BigDecimal("3500.00")))).thenReturn(budget);

        mockMvc.perform(patch("/api/budgets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montoPlaneado\":3500.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.montoPlaneado").value(3500.00));
    }

    @Test
    void creaPresupuestoSemanal() throws Exception {
        PeriodicBudget budget = PeriodicBudget.crearNuevo(
                PeriodicBudget.PeriodType.SEMANAL,
                LocalDate.parse("2026-08-03"),
                LocalDate.parse("2026-08-09"),
                UUID.randomUUID(),
                new BigDecimal("1200.00")
        );
        when(budgetUseCases.crearPeriodico(eq(PeriodicBudget.PeriodType.SEMANAL),
                eq(LocalDate.parse("2026-08-03")),
                eq(LocalDate.parse("2026-08-09")),
                eq(budget.getCategoriaId()),
                eq(new BigDecimal("1200.00")))).thenReturn(budget);

        String payload = """
                {
                  "desde": "2026-08-03",
                  "hasta": "2026-08-09",
                  "categoriaId": "%s",
                  "montoPlaneado": 1200.00
                }
                """.formatted(budget.getCategoriaId());

        mockMvc.perform(post("/api/budgets/weekly")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.periodType").value("SEMANAL"));
    }

    @Test
    void ajustaPresupuestoQuincenal() throws Exception {
        UUID id = UUID.randomUUID();
        PeriodicBudget budget = new PeriodicBudget(
                id,
                PeriodicBudget.PeriodType.QUINCENAL,
                LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-15"),
                UUID.randomUUID(),
                new BigDecimal("2200.00")
        );

        when(budgetUseCases.ajustarPeriodico(eq(id), eq(new BigDecimal("2200.00")))).thenReturn(budget);

        mockMvc.perform(patch("/api/budgets/biweekly/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montoPlaneado\":2200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.periodType").value("QUINCENAL"));
    }

    @Test
    void reportaPresupuestoVsReal() throws Exception {
        BudgetVsActualReport report = new BudgetVsActualReport(
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z"),
                PeriodScope.TODOS,
                new BigDecimal("10000.00"),
                new BigDecimal("7300.00"),
                new BigDecimal("2700.00"),
                List.of(new CategoryBudgetDelta(UUID.randomUUID(), new BigDecimal("3000.00"), new BigDecimal("2500.00")))
        );
        when(budgetUseCases.presupuestoVsReal(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")),
                eq(PeriodScope.TODOS)
        )).thenReturn(report);

        mockMvc.perform(get("/api/budgets/vs-actual")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diferencia").value(2700.00))
                .andExpect(jsonPath("$.porCategoria[0].planeado").value(3000.00));
    }
}
