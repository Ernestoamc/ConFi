package com.confi.domain.port.out;

import com.confi.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de salida: el dominio depende de esta interfaz, NO de JPA.
 * El adapter de persistencia (adapter/out/persistence) la implementa.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    List<Account> findAllActive();
}