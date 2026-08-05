package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.TransactionEntity;
import com.confi.adapter.out.persistence.mapper.TransactionPersistenceMapper;
import com.confi.domain.model.Transaction;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionPersistenceMapper mapper;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity saved = jpaRepository.save(mapper.toEntity(transaction));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findByPeriodo(Instant desde, Instant hasta) {
        return jpaRepository.findByFechaBetween(desde, hasta).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Transaction> findByCategoriaAndPeriodo(UUID categoriaId, Instant desde, Instant hasta) {
        return jpaRepository.findByCategoriaIdAndFechaBetween(categoriaId, desde, hasta)
                .stream().map(mapper::toDomain).toList();
    }
}