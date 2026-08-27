package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.CashDtos.CashEntryResponse;
import com.confi.adapter.in.web.dto.CashDtos.RegisterCashInformativeRequest;
import com.confi.adapter.in.web.dto.CashDtos.RegisterCashWithdrawalRequest;
import com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse;
import com.confi.adapter.in.web.mapper.TransactionWebMapper;
import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.CashUseCases;
import com.confi.domain.port.in.CashUseCases.CashInformativoCommand;
import com.confi.domain.port.in.CashUseCases.RetiroEfectivoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/cash")
@Validated
public class CashController {

    private final CashUseCases cashUseCases;
    private final TransactionWebMapper transactionWebMapper;

    public CashController(CashUseCases cashUseCases, TransactionWebMapper transactionWebMapper) {
        this.cashUseCases = cashUseCases;
        this.transactionWebMapper = transactionWebMapper;
    }

    @PostMapping("/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse registrarRetiro(@Valid @RequestBody RegisterCashWithdrawalRequest request) {
        Transaction tx = cashUseCases.registrarRetiro(new RetiroEfectivoCommand(
                request.cuentaOrigenId(),
                request.categoriaId(),
                request.monto(),
                request.nota(),
                request.fecha()
        ));
        return transactionWebMapper.toResponse(tx);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public CashEntryResponse registrarInformativo(@Valid @RequestBody RegisterCashInformativeRequest request) {
        CashEntry cashEntry = cashUseCases.registrarInformativo(new CashInformativoCommand(
                request.movimiento(),
                request.monto(),
                request.nota(),
                request.categoriaId(),
                request.contraparte(),
                request.fecha()
        ));
        return toResponse(cashEntry);
    }

    @GetMapping("/transactions")
    public List<CashEntryResponse> listarInformativos(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta) {
        return cashUseCases.listarInformativos(desde, hasta).stream().map(this::toResponse).toList();
    }

    private CashEntryResponse toResponse(CashEntry cashEntry) {
        return new CashEntryResponse(
                cashEntry.getId(),
                cashEntry.getFecha(),
                cashEntry.getMovimiento(),
                cashEntry.getMonto(),
                cashEntry.getNota(),
                cashEntry.getCategoriaId(),
                cashEntry.getContraparte(),
                cashEntry.isImpactaSaldo()
        );
    }
}
