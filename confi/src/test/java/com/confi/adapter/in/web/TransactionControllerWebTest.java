package com.confi.adapter.in.web;

import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.SaldoInsuficienteException;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.TransactionMaintenanceUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
    private RegisterTransactionUseCase registerTransactionUseCase;

        @MockitoBean
        private TransactionQueryUseCase transactionQueryUseCase;

        @MockitoBean
        private TransactionMaintenanceUseCase transactionMaintenanceUseCase;

        @MockitoBean
    private TransactionWebMapper mapper;

    @Test
    void devuelve400CuandoElBodyEsInvalido() throws Exception {
        String payload = """
                {
                  "monto": 100.00,
                  "nota": "Cena",
                  "cuentaOrigenId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El request contiene campos inválidos"))
                .andExpect(jsonPath("$.detalles").exists());
    }

    @Test
    void devuelve422CuandoNoHaySaldoSuficiente() throws Exception {
        when(registerTransactionUseCase.execute(any()))
                .thenThrow(new SaldoInsuficienteException("Saldo insuficiente"));

        String payload = validPayload();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Saldo insuficiente"));
    }

            @Test
            void listaTransaccionesPorPeriodo() throws Exception {
            UUID cuentaId = UUID.randomUUID();
            Instant fecha = Instant.parse("2026-08-10T12:00:00Z");
            Transaction tx = Transaction.gasto(new BigDecimal("100.00"), "Cena", cuentaId,
                UUID.randomUUID(), null, null, fecha);
            tx.registrarSaldosResultantes(new BigDecimal("900.00"), null);

            when(transactionQueryUseCase.listar(eq(Instant.parse("2026-08-01T00:00:00Z")),
                eq(Instant.parse("2026-08-31T23:59:59Z")), eq(cuentaId)))
                .thenReturn(List.of(tx));

            when(mapper.toResponse(tx)).thenReturn(new com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse(
                tx.getId(), tx.getFecha(), tx.getMonto(), tx.getNota(), tx.getTipo(), tx.getCuentaOrigenId(),
                tx.getCuentaDestinoId(), tx.getCategoriaId(), tx.getContraparte(),
                tx.getSaldoOrigenDespues(), tx.getSaldoDestinoDespues()
            ));

            mockMvc.perform(get("/api/transactions")
                    .param("desde", "2026-08-01T00:00:00Z")
                    .param("hasta", "2026-08-31T23:59:59Z")
                    .param("cuentaId", cuentaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monto").value(100.00))
                .andExpect(jsonPath("$[0].saldoOrigenDespues").value(900.00));
            }

            @Test
            void actualizaNotaDeTransaccion() throws Exception {
            UUID cuentaId = UUID.randomUUID();
            Transaction tx = Transaction.gasto(new BigDecimal("120.00"), "Nueva nota", cuentaId,
                UUID.randomUUID(), null, null, Instant.parse("2026-08-10T12:00:00Z"));
            tx.registrarSaldosResultantes(new BigDecimal("880.00"), null);

            when(transactionMaintenanceUseCase.actualizarNota(eq(tx.getId()), eq("Nueva nota"))).thenReturn(tx);
            when(mapper.toResponse(tx)).thenReturn(new com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse(
                tx.getId(), tx.getFecha(), tx.getMonto(), tx.getNota(), tx.getTipo(), tx.getCuentaOrigenId(),
                tx.getCuentaDestinoId(), tx.getCategoriaId(), tx.getContraparte(),
                tx.getSaldoOrigenDespues(), tx.getSaldoDestinoDespues()
            ));

            mockMvc.perform(patch("/api/transactions/{id}", tx.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nota\":\"Nueva nota\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value("Nueva nota"));
            }

    private static String validPayload() {
        return """
                {
                  "tipo": "GASTO",
                  "monto": 100.00,
                  "nota": "Cena",
                  "cuentaOrigenId": "%s",
                  "categoriaId": "%s"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
    }
}
