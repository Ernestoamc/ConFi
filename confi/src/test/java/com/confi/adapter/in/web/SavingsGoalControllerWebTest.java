package com.confi.adapter.in.web;

import com.confi.domain.service.SavingsGoalService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SavingsGoalController.class)
class SavingsGoalControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingsGoalService service;

    @Test
    void creaYListaMetas() throws Exception {
        SavingsGoalService.SavingsGoal goal = new SavingsGoalService.SavingsGoal(
                UUID.randomUUID(), "Fondo de emergencia", new BigDecimal("10000.00"),
                new BigDecimal("1500.00"), Instant.parse("2027-01-01T00:00:00Z"), true);

        when(service.create(eq("Fondo de emergencia"), eq(new BigDecimal("10000.00")), eq(Instant.parse("2027-01-01T00:00:00Z"))))
                .thenReturn(goal);
        when(service.list()).thenReturn(List.of(goal));

        mockMvc.perform(post("/api/savings-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name":"Fondo de emergencia",
                              "targetAmount":10000.00,
                              "targetDate":"2027-01-01T00:00:00Z"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fondo de emergencia"));

        mockMvc.perform(get("/api/savings-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentAmount").value(1500.00));
    }

    @Test
    void agregaProgreso() throws Exception {
        UUID id = UUID.randomUUID();
        SavingsGoalService.SavingsGoal goal = new SavingsGoalService.SavingsGoal(
                id, "Vacaciones", new BigDecimal("5000.00"), new BigDecimal("2000.00"), null, true);

        when(service.addProgress(eq(id), eq(new BigDecimal("500.00")))).thenReturn(goal);

        mockMvc.perform(patch("/api/savings-goals/{id}/progress", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"amount\":500.00" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vacaciones"));
    }
}
