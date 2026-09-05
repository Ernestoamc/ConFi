package com.confi.domain.port.out;

import com.confi.domain.model.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {

    Budget save(Budget budget);

    Optional<Budget> findById(UUID id);

    List<Budget> findByMesAndAnio(int mes, int anio);

    Optional<Budget> findByCategoriaMesAnio(UUID categoriaId, int mes, int anio);
}