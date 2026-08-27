package com.confi.domain.service;

import com.confi.domain.model.Account;
import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.out.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class AccountService implements AccountUseCases {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account crear(CreateAccountCommand command) {
        Account account = Account.crearNueva(
                command.nombre(), command.tipo(), command.saldoInicial(), command.limiteCredito(),
                command.diaCorte(), command.diaPago());
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> listarActivas() {
        return accountRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public Account obtener(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cuenta no encontrada: " + id));
    }

    @Override
    @Transactional
    public Account renombrar(UUID id, String nuevoNombre) {
        Account account = obtener(id);
        account.renombrar(nuevoNombre);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account desactivar(UUID id) {
        Account account = obtener(id);
        account.desactivar();
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account reactivar(UUID id) {
        Account account = obtener(id);
        account.reactivar();
        return accountRepository.save(account);
    }
}