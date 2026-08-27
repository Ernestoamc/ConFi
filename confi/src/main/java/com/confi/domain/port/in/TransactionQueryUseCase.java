package com.confi.domain.port.in;

import com.confi.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionQueryUseCase {

    enum Movimiento { CARGO, ABONO }

    List<Transaction> listar(Instant desde, Instant hasta, UUID cuentaId);

    AccountStatement estadoCuenta(UUID cuentaId, Instant desde, Instant hasta);

    GeneralStatement estadoGeneral(Instant desde, Instant hasta);

    record StatementEntry(
            UUID transactionId,
            Instant fecha,
            String nota,
            String tipoTransaccion,
            Movimiento movimiento,
            BigDecimal monto,
            BigDecimal saldoDespues,
            UUID cuentaId
    ) {}

    record AccountStatement(
            UUID cuentaId,
            BigDecimal saldoInicial,
            BigDecimal saldoFinal,
            List<StatementEntry> movimientos
    ) {}

    record GeneralStatement(
            BigDecimal saldoInicial,
            BigDecimal saldoFinal,
            List<AccountStatement> porCuenta
    ) {}
}
