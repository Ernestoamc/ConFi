package com.confi.adapter.in.web;

import com.confi.adapter.in.health.BusinessReadinessHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    private BusinessReadinessHealthIndicator businessReadinessHealthIndicator;

    @Test
    void exponeHealthDeNegocioEnActuator() throws Exception {
        when(businessReadinessHealthIndicator.health()).thenReturn(
                Health.up()
                        .withDetail("activeAccounts", 0)
                        .withDetail("activeSubscriptions", 0)
                        .withDetail("chargesInCurrentPeriod", 0)
                        .build()
        );

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
