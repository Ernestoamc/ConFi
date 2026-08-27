package com.confi.adapter.in.web;

import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase.AccountStatement;
import com.confi.domain.port.in.TransactionQueryUseCase.GeneralStatement;
import com.confi.domain.port.in.TransactionQueryUseCase.Movimiento;
import com.confi.domain.port.in.TransactionQueryUseCase.StatementEntry;
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

@WebMvcTest(controllers = StatementController.class)
class StatementControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionQueryUseCase transactionQueryUseCase;

    @Test
    void devuelveEstadoCuentaPorCuenta() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        StatementEntry entry = new StatementEntry(
                UUID.randomUUID(),
                Instant.parse("2026-08-10T12:00:00Z"),
                "Nomina",
                "INGRESO",
                Movimiento.ABONO,
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                cuentaId
        );

        AccountStatement statement = new AccountStatement(
                cuentaId,
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                List.of(entry)
        );

        when(transactionQueryUseCase.estadoCuenta(eq(cuentaId),
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")))).thenReturn(statement);

        mockMvc.perform(get("/api/accounts/{id}/statement", cuentaId)
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoInicial").value(0.00))
                .andExpect(jsonPath("$.saldoFinal").value(1000.00))
                .andExpect(jsonPath("$.movimientos[0].movimiento").value("ABONO"));
    }

    @Test
    void devuelveEstadoGeneralConsolidado() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        AccountStatement accountStatement = new AccountStatement(
                cuentaId,
                new BigDecimal("500.00"),
                new BigDecimal("700.00"),
                List.of()
        );

        when(transactionQueryUseCase.estadoGeneral(
                eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z"))))
                .thenReturn(new GeneralStatement(
                        new BigDecimal("500.00"),
                        new BigDecimal("700.00"),
                        List.of(accountStatement)
                ));

        mockMvc.perform(get("/api/statement")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoInicial").value(500.00))
                .andExpect(jsonPath("$.saldoFinal").value(700.00))
                .andExpect(jsonPath("$.porCuenta[0].cuentaId").value(cuentaId.toString()));
    }
}
