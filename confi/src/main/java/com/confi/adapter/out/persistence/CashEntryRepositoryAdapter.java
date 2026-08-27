package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.mapper.CashEntryPersistenceMapper;
import com.confi.domain.model.CashEntry;
import com.confi.domain.port.out.CashEntryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class CashEntryRepositoryAdapter implements CashEntryRepository {

    private final CashEntryJpaRepository jpaRepository;
    private final CashEntryPersistenceMapper mapper;

    public CashEntryRepositoryAdapter(CashEntryJpaRepository jpaRepository, CashEntryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CashEntry save(CashEntry cashEntry) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(cashEntry)));
    }

    @Override
    public List<CashEntry> findByPeriodo(Instant desde, Instant hasta) {
        return jpaRepository.findByFechaBetween(desde, hasta)
                .stream().map(mapper::toDomain).toList();
    }
}
