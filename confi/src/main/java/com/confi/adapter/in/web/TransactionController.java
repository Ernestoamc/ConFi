package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.TransactionDtos.RegisterTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.UpdateTransactionNoteRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.CancelTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse;
import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.TransactionMaintenanceUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@Validated
public class TransactionController {

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final TransactionQueryUseCase transactionQueryUseCase;
    private final TransactionMaintenanceUseCase transactionMaintenanceUseCase;
    private final TransactionWebMapper mapper;

    public TransactionController(RegisterTransactionUseCase registerTransactionUseCase,
                                  TransactionQueryUseCase transactionQueryUseCase,
                                  TransactionMaintenanceUseCase transactionMaintenanceUseCase,
                                  TransactionWebMapper mapper) {
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.transactionQueryUseCase = transactionQueryUseCase;
        this.transactionMaintenanceUseCase = transactionMaintenanceUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse registrar(@Valid @RequestBody RegisterTransactionRequest request) {
        Transaction registrada = registerTransactionUseCase.execute(new RegisterTransactionCommand(
                request.tipo(),
                request.monto(),
                request.nota(),
                request.cuentaOrigenId(),
                request.cuentaDestinoId(),
                request.categoriaId(),
                request.contraparte(),
                null,
                request.fecha()
        ));
        return mapper.toResponse(registrada);
    }

    @GetMapping
    public List<TransactionResponse> listar(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) UUID cuentaId) {
        return transactionQueryUseCase.listar(desde, hasta, cuentaId)
                .stream().map(mapper::toResponse).toList();
    }

    @PatchMapping("/{id}")
    public TransactionResponse actualizarNota(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateTransactionNoteRequest request) {
        return mapper.toResponse(transactionMaintenanceUseCase.actualizarNota(id, request.nota()));
    }

    @PostMapping("/{id}/cancel")
    public TransactionResponse cancelar(@PathVariable UUID id,
                                        @RequestBody(required = false) CancelTransactionRequest request) {
        String motivo = request == null ? null : request.motivo();
        return mapper.toResponse(transactionMaintenanceUseCase.cancelar(id, motivo));
    }
}