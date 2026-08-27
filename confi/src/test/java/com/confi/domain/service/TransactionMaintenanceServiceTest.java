package com.confi.domain.service;

import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.out.TransactionRepository;
import com.confi.domain.port.out.TransactionReversalRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionMaintenanceServiceTest {

    @Test
    void cancelaTransaccionConReversaIngreso() {
        TransactionRepository txRepo = mock(TransactionRepository.class);
        RegisterTransactionUseCase registerUseCase = mock(RegisterTransactionUseCase.class);
        TransactionReversalRepository reversalRepo = mock(TransactionReversalRepository.class);

        UUID txId = UUID.randomUUID();
        Transaction original = Transaction.gasto(
                new BigDecimal("100.00"),
                "Compra",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                Instant.now()
        );

        Transaction reversa = Transaction.ingreso(
                new BigDecimal("100.00"),
                "Reversa",
                original.getCuentaOrigenId(),
                original.getCategoriaId(),
                null,
                Instant.now()
        );

        when(txRepo.findById(txId)).thenReturn(Optional.of(original));
        when(reversalRepo.existsByOriginalTransactionId(txId)).thenReturn(false);
        when(reversalRepo.existsByReversalTransactionId(txId)).thenReturn(false);
        when(registerUseCase.execute(any(RegisterTransactionCommand.class))).thenReturn(reversa);

        TransactionMaintenanceService service = new TransactionMaintenanceService(txRepo, registerUseCase, reversalRepo);

        Transaction result = service.cancelar(txId, "Error captura");

        assertEquals(TransactionType.INGRESO, result.getTipo());
        assertEquals(new BigDecimal("100.00"), result.getMonto());
    }
}
