package com.confi.domain.port.in;

import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RegisterTransactionUseCase {

    Transaction execute(RegisterTransactionCommand command);

    record RegisterTransactionCommand(
            TransactionType tipo,
            BigDecimal monto,
            String nota,
            UUID cuentaOrigenId,
            UUID cuentaDestinoId,   // solo transferencia entre cuentas propias
            UUID categoriaId,       // solo gasto/ingreso
            String contraparte,     // solo transferencia a terceros
            UUID subscripcionId,
            Instant fecha
    ) {}
}