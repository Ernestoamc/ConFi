package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.model.AccountType;
import com.confi.domain.model.Budget;
import com.confi.domain.model.DomainEvent;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.BudgetRepository;
import com.confi.domain.port.out.DomainEventPublisher;
import com.confi.domain.port.out.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class RegisterTransactionService implements RegisterTransactionUseCase {

    private static final BigDecimal DEFAULT_LOW_BALANCE_THRESHOLD = new BigDecimal("500.00");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final BudgetRepository budgetRepository;
    private final PeriodCloseService periodCloseService;
    private final BigDecimal lowBalanceThreshold;

    public RegisterTransactionService(TransactionRepository transactionRepository,
                                      AccountRepository accountRepository,
                                      DomainEventPublisher domainEventPublisher) {
        this(transactionRepository, accountRepository, domainEventPublisher, null, null, DEFAULT_LOW_BALANCE_THRESHOLD);
    }

    public RegisterTransactionService(TransactionRepository transactionRepository,
                                      AccountRepository accountRepository,
                                      DomainEventPublisher domainEventPublisher,
                                      BudgetRepository budgetRepository,
                                      PeriodCloseService periodCloseService,
                                      BigDecimal lowBalanceThreshold) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.budgetRepository = budgetRepository;
        this.periodCloseService = periodCloseService;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    @Override
    @Transactional
    public Transaction execute(RegisterTransactionCommand command) {
        Instant fecha = command.fecha() != null ? command.fecha() : Instant.now();
        if (periodCloseService != null) {
            periodCloseService.ensureOpen(fecha, "registro de transacciones");
        }

        Transaction transaction = switch (command.tipo()) {
            case GASTO -> Transaction.gasto(command.monto(), command.nota(),
                    command.cuentaOrigenId(), command.categoriaId(), command.contraparte(), 
                    command.subscripcionId(), fecha);
            case INGRESO -> Transaction.ingreso(command.monto(), command.nota(),
                    command.cuentaOrigenId(), command.categoriaId(), command.contraparte(), fecha);
            case TRANSFERENCIA -> command.cuentaDestinoId() != null
                    ? Transaction.transferenciaEntreCuentasPropias(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.cuentaDestinoId(), fecha)
                    : Transaction.transferenciaATercero(command.monto(), command.nota(),
                        command.cuentaOrigenId(), command.contraparte(), fecha);
        };

        aplicarEfectosEnCuentasYRegistrarSaldos(transaction);
        Transaction saved = transactionRepository.save(transaction);
        publicarEventoTransaccionCreada(saved);
        publicarAlertaExcesoPresupuestoSiAplica(saved);
        return saved;
    }

    private void publicarEventoTransaccionCreada(Transaction transaction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transactionId", transaction.getId().toString());
        payload.put("type", transaction.getTipo().name());
        payload.put("amount", transaction.getMonto().toPlainString());
        payload.put("accountId", transaction.getCuentaOrigenId().toString());
        payload.put("occurredAt", transaction.getFecha().toString());
        if (transaction.getSubscripcionId() != null) {
            payload.put("subscriptionId", transaction.getSubscripcionId().toString());
        }

        DomainEvent event = DomainEvent.create("transaction.created", payload);
        domainEventPublisher.publish(event);
    }

    private void aplicarEfectosEnCuentasYRegistrarSaldos(Transaction transaction) {
        Account cuentaOrigen = buscarCuenta(transaction.getCuentaOrigenId());

        switch (transaction.getTipo()) {
            case GASTO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);
                transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
                publicarAlertaSaldoBajoSiAplica(cuentaOrigen);
            }
            case INGRESO -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto());
                accountRepository.save(cuentaOrigen);
                transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
                publicarAlertaSaldoBajoSiAplica(cuentaOrigen);
            }
            case TRANSFERENCIA -> {
                cuentaOrigen.aplicarMovimiento(transaction.getMonto().negate());
                accountRepository.save(cuentaOrigen);
                publicarAlertaSaldoBajoSiAplica(cuentaOrigen);

                if (transaction.esTransferenciaInterna()) {
                    Account cuentaDestino = buscarCuenta(transaction.getCuentaDestinoId());
                    cuentaDestino.aplicarMovimiento(transaction.getMonto());
                    accountRepository.save(cuentaDestino);
                    transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), cuentaDestino.getSaldo());
                    publicarAlertaSaldoBajoSiAplica(cuentaDestino);
                } else {
                    transaction.registrarSaldosResultantes(cuentaOrigen.getSaldo(), null);
                }
            }
        }
    }

    private void publicarAlertaSaldoBajoSiAplica(Account account) {
        if (account.getTipo() != AccountType.DEBITO) {
            return;
        }
        if (account.getSaldo().compareTo(lowBalanceThreshold) > 0) {
            return;
        }

        DomainEvent event = DomainEvent.create("account.low.balance", Map.of(
                "accountId", account.getId().toString(),
                "accountName", account.getNombre(),
                "balance", account.getSaldo().toPlainString(),
                "threshold", lowBalanceThreshold.toPlainString(),
                "occurredAt", Instant.now().toString()
        ));
        domainEventPublisher.publish(event);
    }

    private void publicarAlertaExcesoPresupuestoSiAplica(Transaction transaction) {
        if (budgetRepository == null) {
            return;
        }
        if (transaction.getTipo() != TransactionType.GASTO || transaction.getCategoriaId() == null) {
            return;
        }

        LocalDate txDate = transaction.getFecha().atZone(ZoneOffset.UTC).toLocalDate();
        YearMonth ym = YearMonth.from(txDate);
        budgetRepository.findByCategoriaMesAnio(transaction.getCategoriaId(), ym.getMonthValue(), ym.getYear())
                .ifPresent(budget -> publicarEventoPresupuestoExcedidoSiCorresponde(transaction, budget));
    }

    private void publicarEventoPresupuestoExcedidoSiCorresponde(Transaction transaction, Budget budget) {
        YearMonth ym = YearMonth.of(budget.getAnio(), budget.getMes());
        Instant desde = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant hasta = ym.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<Transaction> gastosCategoria = transactionRepository.findByCategoriaAndPeriodo(
                budget.getCategoriaId(), desde, hasta);

        BigDecimal real = gastosCategoria.stream()
                .filter(tx -> tx.getTipo() == TransactionType.GASTO)
                .map(Transaction::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (real.compareTo(budget.getMontoPlaneado()) <= 0) {
            return;
        }

        domainEventPublisher.publish(DomainEvent.create("budget.threshold.exceeded", Map.of(
                "budgetId", budget.getId().toString(),
                "categoryId", budget.getCategoriaId().toString(),
                "periodMonth", budget.getMes(),
                "periodYear", budget.getAnio(),
                "planned", budget.getMontoPlaneado().toPlainString(),
                "actual", real.toPlainString(),
                "difference", real.subtract(budget.getMontoPlaneado()).toPlainString(),
                "triggerTransactionId", transaction.getId().toString()
        )));
    }

    private Account buscarCuenta(java.util.UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada: " + id));
    }
}