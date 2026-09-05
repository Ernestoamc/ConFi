package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.PeriodicBudgetEntity;
import com.confi.domain.model.PeriodicBudget;
import org.springframework.stereotype.Component;

@Component
public class PeriodicBudgetPersistenceMapper {

    public PeriodicBudgetEntity toEntity(PeriodicBudget budget) {
        return new PeriodicBudgetEntity(
                budget.getId(),
                PeriodicBudgetEntity.PeriodTypeJpa.valueOf(budget.getPeriodType().name()),
                budget.getDesde(),
                budget.getHasta(),
                budget.getCategoriaId(),
                budget.getMontoPlaneado()
        );
    }

    public PeriodicBudget toDomain(PeriodicBudgetEntity entity) {
        return new PeriodicBudget(
                entity.getId(),
                PeriodicBudget.PeriodType.valueOf(entity.getPeriodType().name()),
                entity.getDesde(),
                entity.getHasta(),
                entity.getCategoriaId(),
                entity.getMontoPlaneado()
        );
    }
}
