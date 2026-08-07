package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.TransactionDtos.RegisterTransactionRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse;
import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final TransactionWebMapper mapper;

    public TransactionController(RegisterTransactionUseCase registerTransactionUseCase,
                                  TransactionWebMapper mapper) {
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.mapper = mapper;
    }

    /**
     * @Transactional aquí garantiza que si falla el guardado de una de las cuentas
     * involucradas (ej. saldo insuficiente), toda la operación se revierte.
     * La lógica de negocio vive en el dominio; esta anotación es de infraestructura,
     * por eso vive en el adapter y no en el service.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
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
}