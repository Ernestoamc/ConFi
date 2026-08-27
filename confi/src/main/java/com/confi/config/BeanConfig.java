package com.confi.config;

import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.CashUseCases;
import com.confi.domain.port.in.FinancialReportUseCase;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.in.SubscriptionUseCases;
import com.confi.domain.port.in.TransactionMaintenanceUseCase;
import com.confi.domain.port.in.TransactionQueryUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.CashEntryRepository;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import com.confi.domain.port.out.TransactionRepository;
import com.confi.domain.port.out.TransactionReversalRepository;
import com.confi.domain.service.AccountService;
import com.confi.domain.service.CashService;
import com.confi.domain.service.FinancialReportService;
import com.confi.domain.service.GenerateMonthlyChargesService;
import com.confi.domain.service.RegisterTransactionService;
import com.confi.domain.service.SubscriptionChargeService;
import com.confi.domain.service.SubscriptionService;
import com.confi.domain.service.TransactionMaintenanceService;
import com.confi.domain.service.TransactionQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public RegisterTransactionUseCase registerTransactionUseCase(
            TransactionRepository transactionRepository, AccountRepository accountRepository) {
        return new RegisterTransactionService(transactionRepository, accountRepository);
    }

    @Bean
    public AccountUseCases accountUseCases(AccountRepository accountRepository) {
        return new AccountService(accountRepository);
    }

    @Bean
    public SubscriptionUseCases subscriptionUseCases(SubscriptionRepository subscriptionRepository) {
        return new SubscriptionService(subscriptionRepository);
    }

    @Bean
    public GenerateMonthlyChargesUseCase generateMonthlyChargesUseCase(
            SubscriptionRepository subscriptionRepository, SubscriptionChargeRepository chargeRepository) {
        return new GenerateMonthlyChargesService(subscriptionRepository, chargeRepository);
    }

    @Bean
    public SubscriptionChargeUseCases subscriptionChargeUseCases(
            SubscriptionChargeRepository chargeRepository, SubscriptionRepository subscriptionRepository,
            RegisterTransactionUseCase registerTransactionUseCase) {
        return new SubscriptionChargeService(chargeRepository, subscriptionRepository, registerTransactionUseCase);
    }

    @Bean
    public TransactionQueryUseCase transactionQueryUseCase(
            TransactionRepository transactionRepository, AccountRepository accountRepository) {
        return new TransactionQueryService(transactionRepository, accountRepository);
    }

    @Bean
    public CashUseCases cashUseCases(RegisterTransactionUseCase registerTransactionUseCase,
                                     CashEntryRepository cashEntryRepository) {
        return new CashService(registerTransactionUseCase, cashEntryRepository);
    }

    @Bean
    public FinancialReportUseCase financialReportUseCase(TransactionRepository transactionRepository,
                                                         CashEntryRepository cashEntryRepository) {
        return new FinancialReportService(transactionRepository, cashEntryRepository);
    }

    @Bean
    public TransactionMaintenanceUseCase transactionMaintenanceUseCase(
            TransactionRepository transactionRepository,
            RegisterTransactionUseCase registerTransactionUseCase,
            TransactionReversalRepository transactionReversalRepository) {
        return new TransactionMaintenanceService(
                transactionRepository,
                registerTransactionUseCase,
                transactionReversalRepository
        );
    }
}