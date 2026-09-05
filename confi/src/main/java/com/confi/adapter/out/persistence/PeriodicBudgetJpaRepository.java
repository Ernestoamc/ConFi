package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.PeriodicBudgetEntity;
import com.confi.adapter.out.persistence.entity.PeriodicBudgetEntity.PeriodTypeJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PeriodicBudgetJpaRepository extends JpaRepository<PeriodicBudgetEntity, UUID> {

    List<PeriodicBudgetEntity> findByPeriodTypeAndDesdeGreaterThanEqualAndHastaLessThanEqual(
            PeriodTypeJpa periodType,
            LocalDate desde,
            LocalDate hasta);
}
