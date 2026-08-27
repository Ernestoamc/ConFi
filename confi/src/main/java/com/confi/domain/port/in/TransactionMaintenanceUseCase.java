package com.confi.domain.port.in;

import com.confi.domain.model.Transaction;

import java.util.UUID;

public interface TransactionMaintenanceUseCase {

    Transaction actualizarNota(UUID transactionId, String nota);

    Transaction cancelar(UUID transactionId, String motivo);
}
