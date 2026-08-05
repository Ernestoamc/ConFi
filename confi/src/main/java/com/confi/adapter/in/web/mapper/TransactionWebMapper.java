package com.confi.adapter.in.web.mapper;

import com.confi.adapter.in.web.dto.TransactionDtos.TransactionResponse;
import com.confi.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionWebMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFecha(),
                transaction.getMonto(),
                transaction.getNota(),
                transaction.getTipo(),
                transaction.getCuentaOrigenId(),
                transaction.getCuentaDestinoId(),
                transaction.getCategoriaId(),
                transaction.getContraparte()
        );
    }
}