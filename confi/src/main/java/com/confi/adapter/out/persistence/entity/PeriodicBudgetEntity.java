package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "periodic_budgets")
public class PeriodicBudgetEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodTypeJpa periodType;

    @Column(name = "desde", nullable = false)
    private LocalDate desde;

    @Column(name = "hasta", nullable = false)
    private LocalDate hasta;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(name = "monto_planeado", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoPlaneado;

    protected PeriodicBudgetEntity() {
    }

    public PeriodicBudgetEntity(UUID id, PeriodTypeJpa periodType, LocalDate desde,
                                LocalDate hasta, UUID categoriaId, BigDecimal montoPlaneado) {
        this.id = id;
        this.periodType = periodType;
        this.desde = desde;
        this.hasta = hasta;
        this.categoriaId = categoriaId;
        this.montoPlaneado = montoPlaneado;
    }

    public enum PeriodTypeJpa { SEMANAL, QUINCENAL }

    public UUID getId() { return id; }
    public PeriodTypeJpa getPeriodType() { return periodType; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public UUID getCategoriaId() { return categoriaId; }
    public BigDecimal getMontoPlaneado() { return montoPlaneado; }
}
