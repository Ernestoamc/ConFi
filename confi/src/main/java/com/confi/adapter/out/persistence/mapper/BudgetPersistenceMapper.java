package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.BudgetEntity;
import com.confi.domain.model.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetPersistenceMapper {

    public BudgetEntity toEntity(Budget budget) {
        return new BudgetEntity(
                budget.getId(),
                budget.getMes(),
                budget.getAnio(),
                budget.getCategoriaId(),
                budget.getMontoPlaneado()
        );
    }

    public Budget toDomain(BudgetEntity entity) {
        return new Budget(
                entity.getId(),
                entity.getMes(),
                entity.getAnio(),
                entity.getCategoriaId(),
                entity.getMontoPlaneado()
        );
    }
}
