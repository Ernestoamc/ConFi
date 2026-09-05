package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int anio;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(name = "monto_planeado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoPlaneado;

    protected BudgetEntity() {
    }

    public BudgetEntity(UUID id, int mes, int anio, UUID categoriaId, BigDecimal montoPlaneado) {
        this.id = id;
        this.mes = mes;
        this.anio = anio;
        this.categoriaId = categoriaId;
        this.montoPlaneado = montoPlaneado;
    }

    public UUID getId() { return id; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
    public UUID getCategoriaId() { return categoriaId; }
    public BigDecimal getMontoPlaneado() { return montoPlaneado; }
}
