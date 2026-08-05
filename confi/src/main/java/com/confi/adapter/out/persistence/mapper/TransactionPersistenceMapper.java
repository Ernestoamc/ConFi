package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.TransactionEntity;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId(),
                transaction.getFecha(),
                transaction.getMonto(),
                transaction.getNota(),
                TransactionEntity.TransactionTypeJpa.valueOf(transaction.getTipo().name()),
                transaction.getCuentaOrigenId(),
                transaction.getCuentaDestinoId(),
                transaction.getCategoriaId(),
                transaction.getContraparte(),
                transaction.getSubscripcionId()
        );
    }

    /** Reconstruye el objeto de dominio desde la entity, preservando el id original. */
    public Transaction toDomain(TransactionEntity entity) {
        TransactionType tipo = TransactionType.valueOf(entity.getTipo().name());
        return Transaction.reconstruir(
                entity.getId(),
                entity.getFecha(),
                entity.getMonto(),
                entity.getNota(),
                tipo,
                entity.getCuentaOrigenId(),
                entity.getCuentaDestinoId(),
                entity.getCategoriaId(),
                entity.getContraparte(),
                entity.getSubscripcionId()
        );
    }
}