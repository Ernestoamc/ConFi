package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.mapper.BudgetPersistenceMapper;
import com.confi.domain.model.Budget;
import com.confi.domain.port.out.BudgetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetRepositoryAdapter implements BudgetRepository {

    private final BudgetJpaRepository jpaRepository;
    private final BudgetPersistenceMapper mapper;

    public BudgetRepositoryAdapter(BudgetJpaRepository jpaRepository, BudgetPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Budget save(Budget budget) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(budget)));
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Budget> findByMesAndAnio(int mes, int anio) {
        return jpaRepository.findByMesAndAnio(mes, anio).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Budget> findByCategoriaMesAnio(UUID categoriaId, int mes, int anio) {
        return jpaRepository.findByCategoriaIdAndMesAndAnio(categoriaId, mes, anio).map(mapper::toDomain);
    }
}
