package com.confi.adapter.in.web;

import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.TransactionQueryUseCase;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    private final TransactionQueryUseCase transactionQueryUseCase;

    public ExportController(TransactionQueryUseCase transactionQueryUseCase) {
        this.transactionQueryUseCase = transactionQueryUseCase;
    }

    @GetMapping(value = "/transactions.csv", produces = "text/csv")
    public ResponseEntity<String> exportTransactionsCsv(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(required = false) UUID cuentaId) {
        List<Transaction> rows = transactionQueryUseCase.listar(desde, hasta, cuentaId);
        String csv = toCsv(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private String toCsv(List<Transaction> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,fecha,tipo,monto,nota,cuentaOrigenId,cuentaDestinoId,categoriaId,contraparte,saldoOrigenDespues,saldoDestinoDespues\n");
        for (Transaction tx : rows) {
            sb.append(tx.getId()).append(',')
                    .append(tx.getFecha()).append(',')
                    .append(tx.getTipo()).append(',')
                    .append(tx.getMonto()).append(',')
                    .append(csv(tx.getNota())).append(',')
                    .append(tx.getCuentaOrigenId()).append(',')
                    .append(tx.getCuentaDestinoId() == null ? "" : tx.getCuentaDestinoId()).append(',')
                    .append(tx.getCategoriaId() == null ? "" : tx.getCategoriaId()).append(',')
                    .append(csv(tx.getContraparte())).append(',')
                    .append(tx.getSaldoOrigenDespues() == null ? "" : tx.getSaldoOrigenDespues()).append(',')
                    .append(tx.getSaldoDestinoDespues() == null ? "" : tx.getSaldoDestinoDespues())
                    .append('\n');
        }
        return sb.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
