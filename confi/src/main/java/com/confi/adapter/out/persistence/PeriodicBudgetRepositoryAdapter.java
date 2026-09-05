package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.PeriodicBudgetEntity;
import com.confi.adapter.out.persistence.mapper.PeriodicBudgetPersistenceMapper;
import com.confi.domain.model.PeriodicBudget;
import com.confi.domain.port.out.PeriodicBudgetRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PeriodicBudgetRepositoryAdapter implements PeriodicBudgetRepository {

    private final PeriodicBudgetJpaRepository jpaRepository;
    private final PeriodicBudgetPersistenceMapper mapper;

    public PeriodicBudgetRepositoryAdapter(PeriodicBudgetJpaRepository jpaRepository,
                                           PeriodicBudgetPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PeriodicBudget save(PeriodicBudget budget) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(budget)));
    }

    @Override
    public Optional<PeriodicBudget> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PeriodicBudget> findByPeriodTypeAndRange(PeriodicBudget.PeriodType periodType,
                                                          LocalDate desde,
                                                          LocalDate hasta) {
        return jpaRepository.findByPeriodTypeAndDesdeGreaterThanEqualAndHastaLessThanEqual(
                        PeriodicBudgetEntity.PeriodTypeJpa.valueOf(periodType.name()),
                        desde,
                        hasta)
                .stream().map(mapper::toDomain).toList();
    }
}
