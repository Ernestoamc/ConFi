package com.confi.adapter.in.web;

import com.confi.domain.service.PeriodCloseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PeriodCloseController.class)
class PeriodCloseControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeriodCloseService service;

    @Test
    void cierraYListaPeriodo() throws Exception {
        mockMvc.perform(post("/api/period-close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"year\":2026," +
                                "\"month\":8" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("2026-08"))
                .andExpect(jsonPath("$.closed").value(true));

        verify(service).close(YearMonth.of(2026, 8));

        when(service.listClosed()).thenReturn(Set.of(YearMonth.of(2026, 8)));
        mockMvc.perform(get("/api/period-close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("2026-08"));
    }

    @Test
    void reabrePeriodo() throws Exception {
        mockMvc.perform(patch("/api/period-close/reopen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"year\":2026," +
                                "\"month\":8" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closed").value(false));
    }
}
