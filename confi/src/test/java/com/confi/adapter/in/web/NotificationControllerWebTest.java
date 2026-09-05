package com.confi.adapter.in.web;

import com.confi.adapter.in.notifications.NotificationInbox;
import com.confi.adapter.in.notifications.NotificationItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationInbox notificationInbox;

    @Test
    void listaNotificacionesConLimite() throws Exception {
        NotificationItem item = new NotificationItem(
                UUID.randomUUID(),
                "transaction.created",
                Instant.parse("2026-08-28T12:00:00Z"),
                "Nueva transaccion",
                "Se registro una transaccion GASTO por 120.00",
            Map.of("amount", "120.00", "type", "GASTO"),
            false,
            null
        );

        when(notificationInbox.latest(10)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/notifications").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("transaction.created"))
                .andExpect(jsonPath("$[0].title").value("Nueva transaccion"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].payload.amount").value("120.00"));
    }

    @Test
    void retorna400SiLimitEsInvalido() throws Exception {
        mockMvc.perform(get("/api/notifications").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void marcaNotificacionComoLeida() throws Exception {
        UUID id = UUID.randomUUID();
        NotificationItem item = new NotificationItem(
                id,
                "transaction.created",
                Instant.parse("2026-08-28T12:00:00Z"),
                "Nueva transaccion",
                "Se registro una transaccion GASTO por 120.00",
                Map.of("amount", "120.00", "type", "GASTO"),
                true,
                Instant.parse("2026-08-29T00:00:00Z")
        );
        when(notificationInbox.markRead(id)).thenReturn(item);

        mockMvc.perform(patch("/api/notifications/{id}/read", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").value("2026-08-29T00:00:00Z"));
    }

    @Test
    void retorna404CuandoNoExisteNotificacionAMarcar() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationInbox.markRead(id)).thenThrow(new NoSuchElementException("Notificacion no encontrada"));

        mockMvc.perform(patch("/api/notifications/{id}/read", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcaTodasComoLeidas() throws Exception {
        when(notificationInbox.markAllRead()).thenReturn(3);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(3));
    }

    @Test
    void exponeResumenDeNoLeidas() throws Exception {
        when(notificationInbox.unreadCount()).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
    void limpiaBandeja() throws Exception {
        mockMvc.perform(delete("/api/notifications"))
                .andExpect(status().isNoContent());

        verify(notificationInbox).clear();
    }
}
