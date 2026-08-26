package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.SubscriptionChargeEntity;
import com.confi.adapter.out.persistence.mapper.SubscriptionChargePersistenceMapper;
import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.port.out.SubscriptionChargeRepository;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionChargeRepositoryAdapter implements SubscriptionChargeRepository {

    private final SubscriptionChargeJpaRepository jpaRepository;
    private final SubscriptionChargePersistenceMapper mapper;

    public SubscriptionChargeRepositoryAdapter(SubscriptionChargeJpaRepository jpaRepository,
                                                SubscriptionChargePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SubscriptionCharge save(SubscriptionCharge charge) {
        SubscriptionChargeEntity saved = jpaRepository.save(mapper.toEntity(charge));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SubscriptionCharge> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SubscriptionCharge> findByMesAndAnio(int mes, int anio) {
        YearMonth periodo = YearMonth.of(anio, mes);
        return jpaRepository.findByFechaEsperadaBetween(periodo.atDay(1), periodo.atEndOfMonth())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySubscripcionAndMesAnio(UUID subscripcionId, int mes, int anio) {
        YearMonth periodo = YearMonth.of(anio, mes);
        return jpaRepository.existsBySubscripcionIdAndFechaEsperadaBetween(
                subscripcionId, periodo.atDay(1), periodo.atEndOfMonth());
    }

    @Override
    public Optional<SubscriptionCharge> findLastBySubscripcionId(UUID subscripcionId) {
        return jpaRepository.findTopBySubscripcionIdOrderByFechaEsperadaDesc(subscripcionId)
                .map(mapper::toDomain);
    }
}