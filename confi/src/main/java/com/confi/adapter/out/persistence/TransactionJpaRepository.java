package com.confi.adapter.out.persistence;

import com.confi.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByFechaBetween(Instant desde, Instant hasta);

        @Query("""
            select t
            from TransactionEntity t
            where t.fecha between :desde and :hasta
              and (t.cuentaOrigenId = :cuentaId or t.cuentaDestinoId = :cuentaId)
            """)
        List<TransactionEntity> findByCuentaAndFechaBetween(
            @Param("cuentaId") UUID cuentaId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    List<TransactionEntity> findByCategoriaIdAndFechaBetween(UUID categoriaId, Instant desde, Instant hasta);
}