package com.confi.domain.port.out;

import com.confi.domain.model.PeriodicBudget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodicBudgetRepository {

    PeriodicBudget save(PeriodicBudget budget);

    Optional<PeriodicBudget> findById(UUID id);

    List<PeriodicBudget> findByPeriodTypeAndRange(PeriodicBudget.PeriodType periodType,
                                                   LocalDate desde,
                                                   LocalDate hasta);
}
