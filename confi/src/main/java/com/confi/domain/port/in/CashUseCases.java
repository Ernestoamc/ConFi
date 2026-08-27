package com.confi.domain.port.in;

import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CashUseCases {

    Transaction registrarRetiro(RetiroEfectivoCommand command);

    CashEntry registrarInformativo(CashInformativoCommand command);

    List<CashEntry> listarInformativos(Instant desde, Instant hasta);

    record RetiroEfectivoCommand(
            UUID cuentaOrigenId,
            UUID categoriaId,
            BigDecimal monto,
            String nota,
            Instant fecha
    ) {}

    record CashInformativoCommand(
            CashEntry.Movimiento movimiento,
            BigDecimal monto,
            String nota,
            UUID categoriaId,
            String contraparte,
            Instant fecha
    ) {}
}
