package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.model.AccountType;
import com.confi.domain.model.Budget;
import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.BudgetRepository;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba la ORQUESTACIÓN del servicio
 * ¿se llaman los repositorios correctos, las veces correctas, con los datos correctos?
 */
class RegisterTransactionServiceTest {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
        private DomainEventPublisher domainEventPublisher;
    private RegisterTransactionService service;

    private final UUID categoria = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        domainEventPublisher = mock(DomainEventPublisher.class);
        service = new RegisterTransactionService(transactionRepository, accountRepository, domainEventPublisher);

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void unGastoRestaDeLaCuentaYRegistraElSaldoResultante() {
        Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("1000.00"), null, null, null);
        when(accountRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));

        Transaction resultado = service.execute(new RegisterTransactionCommand(
                TransactionType.GASTO, new BigDecimal("350.50"), "Cena",
                cuenta.getId(), null, categoria, null, null, null));

        assertThat(resultado.getSaldoOrigenDespues()).isEqualByComparingTo("649.50");
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("649.50");
        verify(accountRepository, times(1)).save(cuenta);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void unaTransferenciaEntreCuentasPropiasActualizaAmbasCuentas() {
        Account origen = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("5000.00"), null, null, null);
        Account destino = Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                new BigDecimal("-3000.00"), new BigDecimal("10000.00"), 20, 5);
        when(accountRepository.findById(origen.getId())).thenReturn(Optional.of(origen));
        when(accountRepository.findById(destino.getId())).thenReturn(Optional.of(destino));

        Transaction resultado = service.execute(new RegisterTransactionCommand(
                TransactionType.TRANSFERENCIA, new BigDecimal("1000.00"), "Pago de tarjeta",
                origen.getId(), destino.getId(), null, null, null, null));

        assertThat(origen.getSaldo()).isEqualByComparingTo("4000.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("-2000.00");
        assertThat(resultado.getSaldoOrigenDespues()).isEqualByComparingTo("4000.00");
        assertThat(resultado.getSaldoDestinoDespues()).isEqualByComparingTo("-2000.00");
        verify(accountRepository, times(1)).save(origen);
        verify(accountRepository, times(1)).save(destino);
    }

    @Test
    void unaTransferenciaATerceroSoloActualizaLaCuentaOrigen() {
        Account origen = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("5000.00"), null, null, null);
        when(accountRepository.findById(origen.getId())).thenReturn(Optional.of(origen));

        Transaction resultado = service.execute(new RegisterTransactionCommand(
                TransactionType.TRANSFERENCIA, new BigDecimal("800.00"), "Renta",
                origen.getId(), null, null, "Casero", null, null));

        assertThat(origen.getSaldo()).isEqualByComparingTo("4200.00");
        assertThat(resultado.getSaldoDestinoDespues()).isNull();
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void unIngresoSumaALaCuenta() {
        Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("1000.00"), null, null, null);
        when(accountRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));

        service.execute(new RegisterTransactionCommand(
                TransactionType.INGRESO, new BigDecimal("500.00"), "Nómina",
                cuenta.getId(), null, categoria, null, null, null));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("1500.00");
    }

    @Test
    void siLaCuentaOrigenNoExisteLanzaExcepcionYNoGuardaNada() {
        UUID cuentaInexistente = UUID.randomUUID();
        when(accountRepository.findById(cuentaInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new RegisterTransactionCommand(
                TransactionType.GASTO, new BigDecimal("100"), "Cena",
                cuentaInexistente, null, categoria, null, null, null)))
                .isInstanceOf(NoSuchElementException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void siLaCuentaDestinoNoExisteLanzaExcepcionPeroYaMutoLaOrigenEnMemoria() {
        Account origen = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("5000.00"), null, null, null);
        UUID destinoInexistente = UUID.randomUUID();
        when(accountRepository.findById(origen.getId())).thenReturn(Optional.of(origen));
        when(accountRepository.findById(destinoInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new RegisterTransactionCommand(
                TransactionType.TRANSFERENCIA, new BigDecimal("1000.00"), "x",
                origen.getId(), destinoInexistente, null, null, null, null)))
                .isInstanceOf(NoSuchElementException.class);

        verify(accountRepository, times(1)).save(origen);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void usaLaFechaActualCuandoNoSeEspecificaUna() {
        Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                new BigDecimal("1000.00"), null, null, null);
        when(accountRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        service.execute(new RegisterTransactionCommand(
                TransactionType.GASTO, new BigDecimal("100"), "Cena",
                cuenta.getId(), null, categoria, null, null, null));

        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getFecha()).isNotNull();
    }

        @Test
        void publicaAlertaCuandoSaldoDebitoQuedaPorDebajoDelUmbral() {
                Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                                new BigDecimal("600.00"), null, null, null);
                when(accountRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));

                service.execute(new RegisterTransactionCommand(
                                TransactionType.GASTO, new BigDecimal("150.00"), "Super",
                                cuenta.getId(), null, categoria, null, null, null));

                ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
                verify(domainEventPublisher, atLeast(1)).publish(captor.capture());

                List<DomainEvent> events = captor.getAllValues();
                assertThat(events).extracting(DomainEvent::eventType)
                                .contains("transaction.created", "account.low.balance");
        }

            @Test
            void publicaAlertaCuandoSeExcedePresupuestoDeCategoria() {
                Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                        new BigDecimal("1000.00"), null, null, null);
                when(accountRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));

                BudgetRepository budgetRepository = mock(BudgetRepository.class);
                RegisterTransactionService serviceWithBudget = new RegisterTransactionService(
                        transactionRepository,
                        accountRepository,
                        domainEventPublisher,
                        budgetRepository,
                        null,
                        new BigDecimal("500.00")
                );

                UUID categoriaId = UUID.randomUUID();
                YearMonth ym = YearMonth.now(java.time.ZoneOffset.UTC);
                Budget budget = Budget.crearNuevo(ym.getMonthValue(), ym.getYear(), categoriaId, new BigDecimal("300.00"));
                when(budgetRepository.findByCategoriaMesAnio(categoriaId, ym.getMonthValue(), ym.getYear()))
                        .thenReturn(Optional.of(budget));

                when(transactionRepository.findByCategoriaAndPeriodo(eq(categoriaId), any(), any()))
                        .thenAnswer(inv -> List.of(
                                Transaction.gasto(new BigDecimal("200.00"), "A", cuenta.getId(), categoriaId, null, null, java.time.Instant.now()),
                                Transaction.gasto(new BigDecimal("250.00"), "B", cuenta.getId(), categoriaId, null, null, java.time.Instant.now())
                        ));

                serviceWithBudget.execute(new RegisterTransactionCommand(
                        TransactionType.GASTO, new BigDecimal("250.00"), "B",
                        cuenta.getId(), null, categoriaId, null, null, java.time.Instant.now()));

                ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
                verify(domainEventPublisher, atLeast(1)).publish(captor.capture());
                assertThat(captor.getAllValues()).extracting(DomainEvent::eventType)
                        .contains("budget.threshold.exceeded");
            }
}