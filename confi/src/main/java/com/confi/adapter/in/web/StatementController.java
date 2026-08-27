package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.StatementDtos.AccountStatementResponse;
import com.confi.adapter.in.web.dto.StatementDtos.GeneralStatementResponse;
import com.confi.adapter.in.web.dto.StatementDtos.StatementEntryResponse;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase.AccountStatement;
import com.confi.domain.port.in.TransactionQueryUseCase.GeneralStatement;
import com.confi.domain.port.in.TransactionQueryUseCase.StatementEntry;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@Validated
public class StatementController {

    private final TransactionQueryUseCase transactionQueryUseCase;

    public StatementController(TransactionQueryUseCase transactionQueryUseCase) {
        this.transactionQueryUseCase = transactionQueryUseCase;
    }

    @GetMapping("/api/accounts/{id}/statement")
    public AccountStatementResponse estadoCuenta(
            @PathVariable UUID id,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return toResponse(transactionQueryUseCase.estadoCuenta(id, desde, hasta));
    }

    @GetMapping("/api/statement")
    public GeneralStatementResponse estadoGeneral(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return toResponse(transactionQueryUseCase.estadoGeneral(desde, hasta));
    }

    private AccountStatementResponse toResponse(AccountStatement statement) {
        List<StatementEntryResponse> movimientos = statement.movimientos().stream()
                .map(this::toResponse)
                .toList();
        return new AccountStatementResponse(
                statement.cuentaId(),
                statement.saldoInicial(),
                statement.saldoFinal(),
                movimientos
        );
    }

    private GeneralStatementResponse toResponse(GeneralStatement statement) {
        List<AccountStatementResponse> porCuenta = statement.porCuenta().stream()
                .map(this::toResponse)
                .toList();
        return new GeneralStatementResponse(
                statement.saldoInicial(),
                statement.saldoFinal(),
                porCuenta
        );
    }

    private StatementEntryResponse toResponse(StatementEntry entry) {
        return new StatementEntryResponse(
                entry.transactionId(),
                entry.fecha(),
                entry.nota(),
                entry.tipoTransaccion(),
                entry.movimiento(),
                entry.monto(),
                entry.saldoDespues(),
                entry.cuentaId()
        );
    }
}
