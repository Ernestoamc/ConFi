package com.confi.adapter.in.web;

import com.confi.adapter.in.web.mapper.AccountWebMapper;
import com.confi.domain.model.Account;
import com.confi.domain.model.AccountType;
import com.confi.domain.port.in.AccountUseCases;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
class AccountControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountUseCases accountUseCases;

    @MockitoBean
    private AccountWebMapper mapper;

    @Test
    void renombraCuenta() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = new Account(id, "Nueva", AccountType.DEBITO, new BigDecimal("1000.00"), null, null, null, true);

        when(accountUseCases.renombrar(eq(id), eq("Nueva"))).thenReturn(account);
        when(mapper.toResponse(account)).thenReturn(new com.confi.adapter.in.web.dto.AccountDtos.AccountResponse(
                id, "Nueva", AccountType.DEBITO, new BigDecimal("1000.00"), null, null, null, true
        ));

        mockMvc.perform(patch("/api/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"nombre\":\"Nueva\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nueva"));
    }

    @Test
    void desactivaCuenta() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = new Account(id, "Cuenta", AccountType.DEBITO, new BigDecimal("1000.00"), null, null, null, false);

        when(accountUseCases.desactivar(eq(id))).thenReturn(account);
        when(mapper.toResponse(account)).thenReturn(new com.confi.adapter.in.web.dto.AccountDtos.AccountResponse(
                id, "Cuenta", AccountType.DEBITO, new BigDecimal("1000.00"), null, null, null, false
        ));

        mockMvc.perform(patch("/api/accounts/{id}/desactivar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activa").value(false));
    }
}
