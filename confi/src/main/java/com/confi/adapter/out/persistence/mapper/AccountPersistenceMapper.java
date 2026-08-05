package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.AccountEntity;
import com.confi.domain.model.Account;
import com.confi.domain.model.AccountType;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceMapper {

    public AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.getId(),
                account.getNombre(),
                AccountEntity.AccountTypeJpa.valueOf(account.getTipo().name()),
                account.getSaldo(),
                account.getLimiteCredito(),
                account.isActiva()
        );
    }

    public Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                entity.getNombre(),
                AccountType.valueOf(entity.getTipo().name()),
                entity.getSaldo(),
                entity.getLimiteCredito(),
                entity.isActiva()
        );
    }
}