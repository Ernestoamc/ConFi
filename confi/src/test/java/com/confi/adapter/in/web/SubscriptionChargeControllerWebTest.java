package com.confi.adapter.in.web;

import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionChargeController.class)
class SubscriptionChargeControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateMonthlyChargesUseCase generateMonthlyChargesUseCase;

    @MockitoBean
    private SubscriptionChargeUseCases subscriptionChargeUseCases;

    @Test
    void devuelve400CuandoMesEsInvalido() throws Exception {
        mockMvc.perform(get("/api/subscription-charges")
                        .param("mes", "13")
                        .param("anio", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Parámetros inválidos"))
                .andExpect(jsonPath("$.detalles").exists());
    }

    @Test
    void listaCargosDelMesCuandoParametrosSonValidos() throws Exception {
        SubscriptionCharge charge = SubscriptionCharge.crearPendiente(
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 15),
                new BigDecimal("299.00"));

        when(subscriptionChargeUseCases.listarPorMes(8, 2026)).thenReturn(List.of(charge));

        mockMvc.perform(get("/api/subscription-charges")
                        .param("mes", "8")
                        .param("anio", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].montoEsperado").value(299.00));
    }
}
