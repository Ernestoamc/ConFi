package com.confi.adapter.in.web.dto;

import com.confi.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionDtos {

    public record RegisterTransactionRequest(
            @NotNull TransactionType tipo,
            @NotNull @Positive BigDecimal monto,
            String nota,
            @NotNull UUID cuentaOrigenId,
            UUID cuentaDestinoId,  // transferencia entre cuentas propias
            UUID categoriaId,      // gasto/ingreso
            String contraparte,    // transferencia a terceros
            Instant fecha          // opcional, si es null se usa "ahora"
    ) {}

    public record TransactionResponse(
            UUID id,
            Instant fecha,
            BigDecimal monto,
            String nota,
            TransactionType tipo,
            UUID cuentaOrigenId,
            UUID cuentaDestinoId,
            UUID categoriaId,
            String contraparte
    ) {}
}