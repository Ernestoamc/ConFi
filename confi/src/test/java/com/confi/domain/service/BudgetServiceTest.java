package com.confi.domain.service;

import com.confi.domain.port.out.BudgetRepository;
import com.confi.domain.port.out.PeriodicBudgetRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetServiceTest {

    @Test
    void bloqueaCreacionMensualSiPeriodoEstaCerrado() {
        BudgetRepository budgetRepository = mock(BudgetRepository.class);
        PeriodicBudgetRepository periodicBudgetRepository = mock(PeriodicBudgetRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PeriodCloseService periodCloseService = mock(PeriodCloseService.class);

        when(budgetRepository.findByCategoriaMesAnio(any(), eq(8), eq(2026))).thenReturn(Optional.empty());
        doThrow(new IllegalStateException("Periodo cerrado"))
                .when(periodCloseService)
                .ensureOpen(any(), eq("creacion de presupuesto mensual"));

        BudgetService service = new BudgetService(
                budgetRepository,
                periodicBudgetRepository,
                transactionRepository,
                periodCloseService
        );

        assertThatThrownBy(() -> service.crearMensual(8, 2026, UUID.randomUUID(), new BigDecimal("1000.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Periodo cerrado");
    }
}
