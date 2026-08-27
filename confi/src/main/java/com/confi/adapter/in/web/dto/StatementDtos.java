package com.confi.adapter.in.web.dto;

import com.confi.domain.port.in.TransactionQueryUseCase.Movimiento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StatementDtos {

    public record StatementEntryResponse(
            UUID transactionId,
            Instant fecha,
            String nota,
            String tipoTransaccion,
            Movimiento movimiento,
            BigDecimal monto,
            BigDecimal saldoDespues,
            UUID cuentaId
    ) {}

    public record AccountStatementResponse(
            UUID cuentaId,
            BigDecimal saldoInicial,
            BigDecimal saldoFinal,
            List<StatementEntryResponse> movimientos
    ) {}

    public record GeneralStatementResponse(
            BigDecimal saldoInicial,
            BigDecimal saldoFinal,
            List<AccountStatementResponse> porCuenta
    ) {}
}
