package com.confi.adapter.in.web;

import com.confi.domain.service.CategorizationRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategorizationRuleController.class)
class CategorizationRuleControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategorizationRuleService service;

    @Test
    void creaYListaReglas() throws Exception {
        UUID categoria = UUID.randomUUID();
        CategorizationRuleService.Rule rule = new CategorizationRuleService.Rule(
                UUID.randomUUID(), "uber", categoria, 10, true);

        when(service.create(eq("uber"), eq(categoria), eq(10))).thenReturn(rule);
        when(service.list()).thenReturn(List.of(rule));

        mockMvc.perform(post("/api/categorization-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "keyword": "uber",
                              "categoriaId": "%s",
                              "priority": 10
                            }
                            """.formatted(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyword").value("uber"));

        mockMvc.perform(get("/api/categorization-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value(10));
    }

    @Test
    void activaRegla() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoria = UUID.randomUUID();
        CategorizationRuleService.Rule rule = new CategorizationRuleService.Rule(id, "netflix", categoria, 5, true);
        when(service.activate(id)).thenReturn(rule);

        mockMvc.perform(patch("/api/categorization-rules/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}
