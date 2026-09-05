package com.confi.adapter.in.web;

import com.confi.domain.service.TransactionAttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionAttachmentController.class)
class TransactionAttachmentControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionAttachmentService service;

    @Test
    void agregaYListaAdjuntos() throws Exception {
        UUID txId = UUID.randomUUID();
        TransactionAttachmentService.Attachment att = new TransactionAttachmentService.Attachment(
                UUID.randomUUID(), txId, "ticket.pdf", "application/pdf", "https://files/ticket.pdf", Instant.parse("2026-08-29T10:00:00Z"));

        when(service.add(eq(txId), eq("ticket.pdf"), eq("application/pdf"), eq("https://files/ticket.pdf"))).thenReturn(att);
        when(service.listByTransaction(eq(txId))).thenReturn(List.of(att));

        mockMvc.perform(post("/api/transactions/{transactionId}/attachments", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "fileName":"ticket.pdf",
                              "contentType":"application/pdf",
                              "url":"https://files/ticket.pdf"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("ticket.pdf"));

        mockMvc.perform(get("/api/transactions/{transactionId}/attachments", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("https://files/ticket.pdf"));
    }
}
