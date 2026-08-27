package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.TransactionReversalEntity;
import com.confi.domain.model.TransactionReversal;
import org.springframework.stereotype.Component;

@Component
public class TransactionReversalPersistenceMapper {

    public TransactionReversalEntity toEntity(TransactionReversal reversal) {
        return new TransactionReversalEntity(
                reversal.getId(),
                reversal.getOriginalTransactionId(),
                reversal.getReversalTransactionId(),
                reversal.getCreatedAt()
        );
    }

    public TransactionReversal toDomain(TransactionReversalEntity entity) {
        return new TransactionReversal(
                entity.getId(),
                entity.getOriginalTransactionId(),
                entity.getReversalTransactionId(),
                entity.getCreatedAt()
        );
    }
}
