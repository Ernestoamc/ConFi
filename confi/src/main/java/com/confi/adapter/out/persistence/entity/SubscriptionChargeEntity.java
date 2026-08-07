package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscription_charges")
public class SubscriptionChargeEntity {

    @Id
    private UUID id;

    @Column(name = "subscripcion_id", nullable = false)
    private UUID subscripcionId;

    @Column(name = "fecha_esperada", nullable = false)
    private LocalDate fechaEsperada;

    @Column(name = "monto_esperado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoEsperado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoJpa estado;

    @Column(name = "transaction_id")
    private UUID transactionId;

    protected SubscriptionChargeEntity() {
    }

    public SubscriptionChargeEntity(UUID id, UUID subscripcionId, LocalDate fechaEsperada,
                                     BigDecimal montoEsperado, EstadoJpa estado, UUID transactionId) {
        this.id = id;
        this.subscripcionId = subscripcionId;
        this.fechaEsperada = fechaEsperada;
        this.montoEsperado = montoEsperado;
        this.estado = estado;
        this.transactionId = transactionId;
    }

    public enum EstadoJpa { PENDIENTE, CONFIRMADO, OMITIDO }

    public UUID getId() { return id; }
    public UUID getSubscripcionId() { return subscripcionId; }
    public LocalDate getFechaEsperada() { return fechaEsperada; }
    public BigDecimal getMontoEsperado() { return montoEsperado; }
    public EstadoJpa getEstado() { return estado; }
    public UUID getTransactionId() { return transactionId; }
}