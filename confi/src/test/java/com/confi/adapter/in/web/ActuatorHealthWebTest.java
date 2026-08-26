package com.confi.adapter.in.web;

import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.in.SubscriptionUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "management.endpoint.health.show-details=always",
    "management.endpoint.health.show-components=always"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorHealthWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountUseCases accountUseCases;

    @MockitoBean
    private SubscriptionUseCases subscriptionUseCases;

    @MockitoBean
    private SubscriptionChargeUseCases subscriptionChargeUseCases;

    @Test
    void exponeHealthDeNegocioEnActuator() throws Exception {
        when(accountUseCases.listarActivas()).thenReturn(List.of());
        when(subscriptionUseCases.listarActivas()).thenReturn(List.of());
        when(subscriptionChargeUseCases.listarPorMes(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components['business-readiness'].status").value("UP"))
            .andExpect(jsonPath("$.components['business-readiness'].details.activeAccounts").isNumber())
            .andExpect(jsonPath("$.components['business-readiness'].details.activeSubscriptions").isNumber())
            .andExpect(jsonPath("$.components['business-readiness'].details.chargesInCurrentPeriod").isNumber());
    }
}
