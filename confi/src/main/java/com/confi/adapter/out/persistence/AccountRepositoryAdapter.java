package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.AccountEntity;
import com.confi.adapter.out.persistence.mapper.AccountPersistenceMapper;
import com.confi.domain.model.Account;
import com.confi.domain.port.out.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity saved = jpaRepository.save(mapper.toEntity(account));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAllActive() {
        return jpaRepository.findByActivaTrue().stream().map(mapper::toDomain).toList();
    }
}