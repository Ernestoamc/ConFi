package com.confi.adapter.in.web;

import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.SaldoInsuficienteException;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
