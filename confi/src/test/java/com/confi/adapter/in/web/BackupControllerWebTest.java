package com.confi.adapter.in.web;

import com.confi.adapter.in.notifications.NotificationInbox;
import com.confi.adapter.in.notifications.NotificationItem;
import com.confi.domain.service.CategorizationRuleService;
import com.confi.domain.service.PeriodCloseService;
import com.confi.domain.service.SavingsGoalService;
import com.confi.domain.service.TransactionAttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BackupController.class)
class BackupControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationInbox notificationInbox;

    @MockitoBean
    private CategorizationRuleService categorizationRuleService;

    @MockitoBean
    private SavingsGoalService savingsGoalService;

    @MockitoBean
    private TransactionAttachmentService transactionAttachmentService;

    @MockitoBean
    private PeriodCloseService periodCloseService;

    @Test
    void generaBackupDeNotificaciones() throws Exception {
        NotificationItem item = new NotificationItem(
                UUID.randomUUID(),
                "transaction.created",
                Instant.parse("2026-08-29T10:00:00Z"),
                "Nueva transaccion",
                "Se registro una transaccion GASTO por 100.00",
                Map.of("amount", "100.00"),
                false,
                null
        );
        when(notificationInbox.snapshot()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/backups/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].eventType").value("transaction.created"));
    }

    @Test
    void restauraBackupDeNotificaciones() throws Exception {
        when(notificationInbox.restore(anyList())).thenReturn(1);

        mockMvc.perform(post("/api/restores/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "items": [
                                {
                                  "id": "%s",
                                  "eventType": "transaction.created",
                                  "occurredAt": "2026-08-29T10:00:00Z",
                                  "title": "Nueva transaccion",
                                  "message": "ok",
                                  "payload": {"amount":"100.00"},
                                  "read": false,
                                  "readAt": null
                                }
                              ]
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restoredCount").value(1));

        verify(notificationInbox).restore(anyList());
    }

      @Test
      void generaBackupIntegral() throws Exception {
        when(notificationInbox.snapshot()).thenReturn(List.of());
        when(categorizationRuleService.snapshot()).thenReturn(List.of());
        when(savingsGoalService.snapshot()).thenReturn(List.of());
        when(transactionAttachmentService.listAll()).thenReturn(List.of());
        when(periodCloseService.listClosed()).thenReturn(Set.of(YearMonth.of(2026, 8)));

        mockMvc.perform(get("/api/backups/system"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.closedPeriods[0]").value("2026-08"));
      }

      @Test
      void restauraBackupIntegral() throws Exception {
        when(notificationInbox.restore(anyList())).thenReturn(1);
        when(categorizationRuleService.restore(anyList())).thenReturn(2);
        when(savingsGoalService.restore(anyList())).thenReturn(3);
        when(transactionAttachmentService.restore(anyList())).thenReturn(4);
        when(periodCloseService.restoreClosed(anySet())).thenReturn(1);

        mockMvc.perform(post("/api/restores/system")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {
                    "notifications": [],
                    "rules": [],
                    "goals": [],
                    "attachments": [],
                    "closedPeriods": ["2026-08"]
                  }
                  """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notifications").value(1))
            .andExpect(jsonPath("$.rules").value(2))
            .andExpect(jsonPath("$.goals").value(3))
            .andExpect(jsonPath("$.attachments").value(4))
            .andExpect(jsonPath("$.closedPeriods").value(1));
      }
}
