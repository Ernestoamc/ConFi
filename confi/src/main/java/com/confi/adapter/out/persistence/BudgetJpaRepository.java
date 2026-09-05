package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, UUID> {

    List<BudgetEntity> findByMesAndAnio(int mes, int anio);

    Optional<BudgetEntity> findByCategoriaIdAndMesAndAnio(UUID categoriaId, int mes, int anio);
}
