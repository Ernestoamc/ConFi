package com.confi.adapter.out.persistence.mapper;

import com.confi.adapter.out.persistence.entity.CashEntryEntity;
import com.confi.domain.model.CashEntry;
import org.springframework.stereotype.Component;

@Component
public class CashEntryPersistenceMapper {

    public CashEntryEntity toEntity(CashEntry cashEntry) {
        return new CashEntryEntity(
                cashEntry.getId(),
                cashEntry.getFecha(),
                CashEntryEntity.MovimientoJpa.valueOf(cashEntry.getMovimiento().name()),
                cashEntry.getMonto(),
                cashEntry.getNota(),
                cashEntry.getCategoriaId(),
                cashEntry.getContraparte(),
                cashEntry.isImpactaSaldo()
        );
    }

    public CashEntry toDomain(CashEntryEntity entity) {
        return new CashEntry(
                entity.getId(),
                entity.getFecha(),
                CashEntry.Movimiento.valueOf(entity.getMovimiento().name()),
                entity.getMonto(),
                entity.getNota(),
                entity.getCategoriaId(),
                entity.getContraparte(),
                entity.isImpactaSaldo()
        );
    }
}
