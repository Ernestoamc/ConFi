package com.confi.adapter.in.web.mapper;

import com.confi.adapter.in.web.dto.AccountDtos.AccountResponse;
import com.confi.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getNombre(),
                account.getTipo(),
                account.getSaldo(),
                account.getLimiteCredito(),
                account.isActiva()
        );
    }
}