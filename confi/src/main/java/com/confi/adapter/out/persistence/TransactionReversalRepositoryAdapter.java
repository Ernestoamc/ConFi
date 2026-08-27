package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.mapper.TransactionReversalPersistenceMapper;
import com.confi.domain.model.TransactionReversal;
import com.confi.domain.port.out.TransactionReversalRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionReversalRepositoryAdapter implements TransactionReversalRepository {

    private final TransactionReversalJpaRepository jpaRepository;
    private final TransactionReversalPersistenceMapper mapper;

    public TransactionReversalRepositoryAdapter(TransactionReversalJpaRepository jpaRepository,
                                                TransactionReversalPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TransactionReversal save(TransactionReversal reversal) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(reversal)));
    }

    @Override
    public boolean existsByOriginalTransactionId(UUID originalTransactionId) {
        return jpaRepository.existsByOriginalTransactionId(originalTransactionId);
    }

    @Override
    public boolean existsByReversalTransactionId(UUID reversalTransactionId) {
        return jpaRepository.existsByReversalTransactionId(reversalTransactionId);
    }
}
