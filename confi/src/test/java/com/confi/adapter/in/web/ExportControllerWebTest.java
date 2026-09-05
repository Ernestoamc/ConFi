package com.confi.adapter.in.web;

import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.TransactionQueryUseCase;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExportController.class)
class ExportControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionQueryUseCase transactionQueryUseCase;

    @Test
    void exportaTransaccionesCsv() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        Transaction tx = Transaction.gasto(new BigDecimal("100.00"), "Cafe", cuentaId,
                UUID.randomUUID(), null, null, Instant.parse("2026-08-29T10:00:00Z"));
        tx.registrarSaldosResultantes(new BigDecimal("900.00"), null);

        when(transactionQueryUseCase.listar(eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")), eq(cuentaId)))
                .thenReturn(List.of(tx));

        mockMvc.perform(get("/api/exports/transactions.csv")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z")
                        .param("cuentaId", cuentaId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=transactions.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cafe")));
    }
}
