package com.confi.adapter.in.web.dto;

import com.confi.domain.model.CashEntry;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CashDtos {

    public record RegisterCashWithdrawalRequest(
            @NotNull UUID cuentaOrigenId,
            @NotNull UUID categoriaId,
            @NotNull @Positive BigDecimal monto,
            String nota,
            Instant fecha
    ) {}

    public record RegisterCashInformativeRequest(
            @NotNull CashEntry.Movimiento movimiento,
            @NotNull @Positive BigDecimal monto,
            String nota,
            UUID categoriaId,
            String contraparte,
            Instant fecha
    ) {}

    public record CashEntryResponse(
            UUID id,
            Instant fecha,
            CashEntry.Movimiento movimiento,
            BigDecimal monto,
            String nota,
            UUID categoriaId,
            String contraparte,
            boolean impactaSaldo
    ) {}
}
