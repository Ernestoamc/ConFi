package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "monto_estimado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrecuenciaJpa frecuencia;

    @Column(name = "dia_cobro", nullable = false)
    private int diaCobro;

    @Column(name = "cuenta_id", nullable = false)
    private UUID cuentaId;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(nullable = false)
    private boolean activa;

    protected SubscriptionEntity() {
    }

    public SubscriptionEntity(UUID id, String nombre, BigDecimal montoEstimado, FrecuenciaJpa frecuencia,
                               int diaCobro, UUID cuentaId, UUID categoriaId, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.montoEstimado = montoEstimado;
        this.frecuencia = frecuencia;
        this.diaCobro = diaCobro;
        this.cuentaId = cuentaId;
        this.categoriaId = categoriaId;
        this.activa = activa;
    }

    public enum FrecuenciaJpa { MENSUAL, BIMESTRAL, ANUAL }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getMontoEstimado() { return montoEstimado; }
    public FrecuenciaJpa getFrecuencia() { return frecuencia; }
    public int getDiaCobro() { return diaCobro; }
    public UUID getCuentaId() { return cuentaId; }
    public UUID getCategoriaId() { return categoriaId; }
    public boolean isActiva() { return activa; }
}