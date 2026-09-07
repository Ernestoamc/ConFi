package com.confi.domain.service;

import com.confi.domain.model.Transaction;
import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.out.CashEntryRepository;
import com.confi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinancialReportServiceTest {

    @Test
    void calculaInsightsConTopCategoriasYTasaAhorro() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CashEntryRepository cashEntryRepository = mock(CashEntryRepository.class);

        UUID cuenta = UUID.randomUUID();
        UUID categoriaA = UUID.randomUUID();
        UUID categoriaB = UUID.randomUUID();

        Instant desde = Instant.parse("2026-08-01T00:00:00Z");
        Instant hasta = Instant.parse("2026-08-31T23:59:59Z");

        Transaction ingreso = Transaction.ingreso(new BigDecimal("10000.00"), "Nomina", cuenta, categoriaA, null, desde.plusSeconds(60));
        Transaction gastoA1 = Transaction.gasto(new BigDecimal("2500.00"), "Super", cuenta, categoriaA, null, null, desde.plusSeconds(120));
        Transaction gastoA2 = Transaction.gasto(new BigDecimal("1000.00"), "Super2", cuenta, categoriaA, null, null, desde.plusSeconds(180));
        Transaction gastoB = Transaction.gasto(new BigDecimal("3800.00"), "Renta", cuenta, categoriaB, null, null, desde.plusSeconds(240));

        when(transactionRepository.findByPeriodo(eq(desde), eq(hasta))).thenReturn(List.of(ingreso, gastoA1, gastoA2, gastoB));

        FinancialReportService service = new FinancialReportService(transactionRepository, cashEntryRepository);
        FinancialReportUseCase.InsightsReport report = service.insights(desde, hasta, null, 2);

        assertThat(report.totalIngresos()).isEqualByComparingTo("10000.00");
        assertThat(report.totalGastos()).isEqualByComparingTo("7300.00");
        assertThat(report.resultadoNeto()).isEqualByComparingTo("2700.00");
        assertThat(report.tasaAhorro()).isEqualByComparingTo("0.2700");
        assertThat(report.topCategoriasGasto()).hasSize(2);
        assertThat(report.topCategoriasGasto().get(0).categoriaId()).isEqualTo(categoriaB);
        assertThat(report.topCategoriasGasto().get(0).gastoTotal()).isEqualByComparingTo("3800.00");
        assertThat(report.recomendaciones()).isNotEmpty();
    }
}
