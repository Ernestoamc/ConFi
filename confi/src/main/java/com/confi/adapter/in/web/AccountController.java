package com.confi.adapter.in.web;

import com.confi.adapter.in.web.dto.AccountDtos.AccountResponse;
import com.confi.adapter.in.web.dto.AccountDtos.CreateAccountRequest;
import com.confi.adapter.in.web.mapper.AccountWebMapper;
import com.confi.domain.model.Account;
import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.AccountUseCases.CreateAccountCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountUseCases accountUseCases;
    private final AccountWebMapper mapper;

    public AccountController(AccountUseCases accountUseCases, AccountWebMapper mapper) {
        this.accountUseCases = accountUseCases;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse crear(@Valid @RequestBody CreateAccountRequest request) {
        Account creada = accountUseCases.crear(new CreateAccountCommand(
        request.nombre(), request.tipo(), request.saldoInicial(), request.limiteCredito(),
        request.diaCorte(), request.diaPago()));
        return mapper.toResponse(creada);
    }

    @GetMapping
    public List<AccountResponse> listarActivas() {
        return accountUseCases.listarActivas().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse obtener(@PathVariable UUID id) {
        return mapper.toResponse(accountUseCases.obtener(id));
    }
}