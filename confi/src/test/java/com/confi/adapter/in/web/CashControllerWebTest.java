package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.TransactionDtos;
import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.CashUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CashController.class)
class CashControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashUseCases cashUseCases;

    @MockitoBean
    private TransactionWebMapper transactionWebMapper;

    @Test
    void creaRetiroEfectivo() throws Exception {
        UUID cuentaId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();

        Transaction tx = Transaction.gasto(
                new BigDecimal("400.00"),
                "Retiro cajero",
                cuentaId,
                categoriaId,
                "EFECTIVO",
                null,
                Instant.parse("2026-08-22T10:00:00Z")
        );
        tx.registrarSaldosResultantes(new BigDecimal("600.00"), null);

        when(cashUseCases.registrarRetiro(any())).thenReturn(tx);
        when(transactionWebMapper.toResponse(tx)).thenReturn(new TransactionDtos.TransactionResponse(
                tx.getId(), tx.getFecha(), tx.getMonto(), tx.getNota(), tx.getTipo(), tx.getCuentaOrigenId(),
                tx.getCuentaDestinoId(), tx.getCategoriaId(), tx.getContraparte(),
                tx.getSaldoOrigenDespues(), tx.getSaldoDestinoDespues()
        ));

        String payload = """
                {
                  "cuentaOrigenId": "%s",
                  "categoriaId": "%s",
                  "monto": 400.00,
                  "nota": "Retiro cajero"
                }
                """.formatted(cuentaId, categoriaId);

        mockMvc.perform(post("/api/cash/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("GASTO"))
                .andExpect(jsonPath("$.saldoOrigenDespues").value(600.00));
    }

    @Test
    void creaMovimientoInformativoEfectivo() throws Exception {
        CashEntry cashEntry = CashEntry.crearInformativo(
                CashEntry.Movimiento.CARGO,
                new BigDecimal("250.00"),
                "Pago luz en efectivo",
                UUID.randomUUID(),
                "CFE",
                Instant.parse("2026-08-23T08:00:00Z")
        );

        when(cashUseCases.registrarInformativo(any())).thenReturn(cashEntry);

        String payload = """
                {
                  "movimiento": "CARGO",
                  "monto": 250.00,
                  "nota": "Pago luz en efectivo",
                  "contraparte": "CFE"
                }
                """;

        mockMvc.perform(post("/api/cash/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movimiento").value("CARGO"))
                .andExpect(jsonPath("$.impactaSaldo").value(false));
    }

    @Test
    void listaMovimientosInformativos() throws Exception {
        CashEntry cashEntry = CashEntry.crearInformativo(
                CashEntry.Movimiento.ABONO,
                new BigDecimal("100.00"),
                "Sobrante caja",
                null,
                null,
                Instant.parse("2026-08-24T09:00:00Z")
        );

        when(cashUseCases.listarInformativos(any(), any())).thenReturn(List.of(cashEntry));

        mockMvc.perform(get("/api/cash/transactions")
                        .param("desde", "2026-08-01T00:00:00Z")
                        .param("hasta", "2026-08-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movimiento").value("ABONO"))
                .andExpect(jsonPath("$[0].monto").value(100.00));
    }
}
