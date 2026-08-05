package com.confi.domain.port.in;

import com.confi.domain.model.Account;
import com.confi.domain.model.AccountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountUseCases {

    Account crear(CreateAccountCommand command);

    List<Account> listarActivas();

    Account obtener(UUID id);

    record CreateAccountCommand(
            String nombre,
            AccountType tipo,
            BigDecimal saldoInicial,
            BigDecimal limiteCredito
    ) {}
}