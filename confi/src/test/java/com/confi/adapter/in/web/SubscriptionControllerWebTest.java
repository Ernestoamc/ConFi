package com.confi.adapter.in.web;

import com.confi.domain.port.in.SubscriptionUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionController.class)
class SubscriptionControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionUseCases subscriptionUseCases;

    @Test
    void reactivaSuscripcion() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/subscriptions/{id}/reactivar", id))
                .andExpect(status().isOk());

        verify(subscriptionUseCases).reactivar(id);
    }
}
