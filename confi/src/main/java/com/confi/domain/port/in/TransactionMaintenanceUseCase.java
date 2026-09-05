package com.confi.domain.port.in;

import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;

import java.util.UUID;

public interface TransactionMaintenanceUseCase {

    Transaction actualizarNota(UUID transactionId, String nota);

    Transaction cancelar(UUID transactionId, String motivo, UUID categoriaReversaId);

    Transaction reemplazar(UUID transactionId, RegisterTransactionCommand nuevoMovimiento, String motivo);
}
