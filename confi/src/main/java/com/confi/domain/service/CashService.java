package com.confi.domain.service;

import com.confi.domain.model.CashEntry;
import com.confi.domain.model.Transaction;
import com.confi.domain.model.TransactionType;
import com.confi.domain.port.in.CashUseCases;
import com.confi.domain.port.in.RegisterTransactionUseCase;
import com.confi.domain.port.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.confi.domain.port.out.CashEntryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class CashService implements CashUseCases {

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final CashEntryRepository cashEntryRepository;

    public CashService(RegisterTransactionUseCase registerTransactionUseCase,
                       CashEntryRepository cashEntryRepository) {
        this.registerTransactionUseCase = registerTransactionUseCase;
        this.cashEntryRepository = cashEntryRepository;
    }

    @Override
    @Transactional
    public Transaction registrarRetiro(RetiroEfectivoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando de retiro es obligatorio");
        }
        return registerTransactionUseCase.execute(new RegisterTransactionCommand(
                TransactionType.GASTO,
                command.monto(),
                (command.nota() == null || command.nota().isBlank()) ? "Retiro en efectivo" : command.nota(),
                command.cuentaOrigenId(),
                null,
                command.categoriaId(),
                "EFECTIVO",
                null,
                command.fecha()
        ));
    }

    @Override
    @Transactional
    public CashEntry registrarInformativo(CashInformativoCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando informativo es obligatorio");
        }
        CashEntry cashEntry = CashEntry.crearInformativo(
                command.movimiento(),
                command.monto(),
                command.nota(),
                command.categoriaId(),
                command.contraparte(),
                command.fecha()
        );
        return cashEntryRepository.save(cashEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashEntry> listarInformativos(Instant desde, Instant hasta) {
        validarPeriodo(desde, hasta);
        return cashEntryRepository.findByPeriodo(desde, hasta).stream()
                .sorted(Comparator.comparing(CashEntry::getFecha).thenComparing(CashEntry::getId))
                .toList();
    }

    private void validarPeriodo(Instant desde, Instant hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parametros 'desde' y 'hasta' son obligatorios");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El parametro 'desde' no puede ser mayor que 'hasta'");
        }
    }
}
