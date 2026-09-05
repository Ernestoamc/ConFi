package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.TransactionDtos.RegisterTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.UpdateTransactionNoteRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.CancelTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.ReplaceTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse;
import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.TransactionMaintenanceUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.service.CategorizationRuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final CategorizationRuleService categorizationRuleService;
    private final TransactionWebMapper mapper;

    public TransactionController(RegisterTransactionUseCase registerTransactionUseCase,
                                 TransactionQueryUseCase transactionQueryUseCase,
                                 TransactionMaintenanceUseCase transactionMaintenanceUseCase,
                                 CategorizationRuleService categorizationRuleService,
                                 TransactionWebMapper mapper) {
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.transactionQueryUseCase = transactionQueryUseCase;
        this.transactionMaintenanceUseCase = transactionMaintenanceUseCase;
        this.categorizationRuleService = categorizationRuleService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse registrar(@Valid @RequestBody RegisterTransactionRequest request) {
        UUID categoriaResuelta = resolverCategoria(request.tipo(), request.nota(), request.contraparte(), request.categoriaId());
        Transaction registrada = registerTransactionUseCase.execute(new RegisterTransactionCommand(
                request.tipo(),
                request.monto(),
                request.nota(),
                request.cuentaOrigenId(),
                request.cuentaDestinoId(),
            categoriaResuelta,
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

        @GetMapping("/search")
        public List<TransactionResponse> search(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) UUID cuentaId,
            @RequestParam(required = false) TransactionType tipo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) String contraparte,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) BigDecimal montoMin,
            @RequestParam(required = false) BigDecimal montoMax) {
        return transactionQueryUseCase.listar(desde, hasta, cuentaId).stream()
            .filter(tx -> tipo == null || tx.getTipo() == tipo)
            .filter(tx -> categoriaId == null || categoriaId.equals(tx.getCategoriaId()))
            .filter(tx -> contraparte == null || (tx.getContraparte() != null
                && tx.getContraparte().toLowerCase().contains(contraparte.toLowerCase())))
            .filter(tx -> texto == null || ((tx.getNota() != null && tx.getNota().toLowerCase().contains(texto.toLowerCase()))
                || (tx.getContraparte() != null && tx.getContraparte().toLowerCase().contains(texto.toLowerCase()))))
            .filter(tx -> montoMin == null || tx.getMonto().compareTo(montoMin) >= 0)
            .filter(tx -> montoMax == null || tx.getMonto().compareTo(montoMax) <= 0)
            .map(mapper::toResponse)
            .toList();
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
        UUID categoriaReversaId = request == null ? null : request.categoriaReversaId();
        return mapper.toResponse(transactionMaintenanceUseCase.cancelar(id, motivo, categoriaReversaId));
        }

        @PutMapping("/{id}")
        public TransactionResponse reemplazar(@PathVariable UUID id,
                                              @Valid @RequestBody ReplaceTransactionRequest request) {
            UUID categoriaResuelta = resolverCategoria(request.tipo(), request.nota(), request.contraparte(), request.categoriaId());
        return mapper.toResponse(transactionMaintenanceUseCase.reemplazar(
                    id,
                    new RegisterTransactionCommand(
                            request.tipo(),
                            request.monto(),
                            request.nota(),
                            request.cuentaOrigenId(),
                            request.cuentaDestinoId(),
                            categoriaResuelta,
                            request.contraparte(),
                            null,
                            request.fecha()
                    ),
                    request.motivoReemplazo()
        ));
    }

        private UUID resolverCategoria(TransactionType tipo, String nota, String contraparte, UUID categoriaIdOriginal) {
            if (categoriaIdOriginal != null || tipo == TransactionType.TRANSFERENCIA) {
                return categoriaIdOriginal;
            }
            String sourceText = (nota == null ? "" : nota) + " " + (contraparte == null ? "" : contraparte);
            UUID resolved = categorizationRuleService.resolveCategory(sourceText);
            if (resolved == null) {
                throw new IllegalArgumentException("categoriaId es obligatorio para gasto/ingreso cuando no hay regla de categorizacion aplicable");
            }
            return resolved;
        }
}