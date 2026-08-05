package com.confi.adapter.in.web.dto;

import com.confi.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountDtos {

    public record CreateAccountRequest(
            @NotBlank String nombre,
            @NotNull AccountType tipo,
            @NotNull @PositiveOrZero BigDecimal saldoInicial,
            BigDecimal limiteCredito // requerido solo si tipo == CREDITO, se valida en el dominio
    ) {}

    public record AccountResponse(
            UUID id,
            String nombre,
            AccountType tipo,
            BigDecimal saldo,
            BigDecimal limiteCredito,
            boolean activa
    ) {}
}