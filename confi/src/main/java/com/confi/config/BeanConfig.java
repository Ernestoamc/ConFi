package com.confi.config;

import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.out.AccountRepository;
import com.confi.domain.port.out.TransactionRepository;
import com.confi.domain.service.AccountService;
import com.confi.domain.service.RegisterTransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El dominio (domain/service) es Java puro, sin anotaciones de Spring.
 * Aquí, y SOLO aquí, se instancia como beans para que Spring pueda inyectarlo
 * en los controllers. Así el dominio nunca depende del framework.
 */
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
}