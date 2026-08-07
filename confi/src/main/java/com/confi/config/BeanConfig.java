package com.confi.config;

import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.in.SubscriptionUseCases;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import com.confi.domain.port.out.SubscriptionRepository;
import com.confi.domain.port.out.TransactionRepository;
import com.confi.domain.service.AccountService;
import com.confi.domain.service.GenerateMonthlyChargesService;
import com.confi.domain.service.RegisterTransactionService;
import com.confi.domain.service.SubscriptionChargeService;
import com.confi.domain.service.SubscriptionService;
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
}