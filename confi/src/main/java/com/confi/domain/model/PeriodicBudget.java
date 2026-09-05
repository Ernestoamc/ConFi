package com.confi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PeriodicBudget {

    public enum PeriodType { SEMANAL, QUINCENAL }

    private final UUID id;
    private final PeriodType periodType;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final UUID categoriaId;
    private BigDecimal montoPlaneado;

    public PeriodicBudget(UUID id, PeriodType periodType, LocalDate desde, LocalDate hasta,
                          UUID categoriaId, BigDecimal montoPlaneado) {
        if (periodType == null) {
            throw new IllegalArgumentException("El tipo de periodo es obligatorio");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("'desde' no puede ser mayor que 'hasta'");
        }
        if (categoriaId == null) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        if (montoPlaneado == null || montoPlaneado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto planeado no puede ser negativo");
        }
        this.id = id;
        this.periodType = periodType;
        this.desde = desde;
        this.hasta = hasta;
        this.categoriaId = categoriaId;
        this.montoPlaneado = montoPlaneado.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public static PeriodicBudget crearNuevo(PeriodType periodType, LocalDate desde, LocalDate hasta,
                                            UUID categoriaId, BigDecimal montoPlaneado) {
        return new PeriodicBudget(UUID.randomUUID(), periodType, desde, hasta, categoriaId, montoPlaneado);
    }

    public void ajustarMonto(BigDecimal nuevoMonto) {
        if (nuevoMonto == null || nuevoMonto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto planeado no puede ser negativo");
        }
        this.montoPlaneado = nuevoMonto.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public UUID getId() { return id; }
    public PeriodType getPeriodType() { return periodType; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public UUID getCategoriaId() { return categoriaId; }
    public BigDecimal getMontoPlaneado() { return montoPlaneado; }
}
