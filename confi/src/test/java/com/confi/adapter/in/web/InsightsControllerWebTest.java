package com.confi.adapter.in.web;

import com.confi.domain.port.in.FinancialReportUseCase;
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

@WebMvcTest(controllers = InsightsController.class)
class InsightsControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialReportUseCase financialReportUseCase;

    @Test
    void generaInsightsConTopCategoriasYRecomendaciones() throws Exception {
        UUID categoria = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(financialReportUseCase.insights(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")),
                eq(null),
                eq(3)
        )).thenReturn(new FinancialReportUseCase.InsightsReport(
                Instant.parse("2026-08-01T00:00:00Z"),
                Instant.parse("2026-08-31T23:59:59Z"),
                null,
                new BigDecimal("10000.00"),
                new BigDecimal("7300.00"),
                new BigDecimal("2700.00"),
                new BigDecimal("0.2700"),
                List.of(new FinancialReportUseCase.CategorySpendInsight(
                        categoria,
                        new BigDecimal("3200.00"),
                        new BigDecimal("0.4384")
                )),
                List.of("Una sola categoria concentra mas del 40% del gasto; revisa suscripciones y gastos recurrentes en esa categoria.")
        ));

        mockMvc.perform(get("/api/insights")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z")
                        .param("top", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasaAhorro").value(0.27))
                .andExpect(jsonPath("$.topCategoriasGasto[0].categoriaId").value(categoria.toString()))
                .andExpect(jsonPath("$.recomendaciones[0]").exists());
    }
}
