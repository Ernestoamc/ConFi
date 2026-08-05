package com.confi.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Presupuesto planeado para una categoría en un mes/año específico.
 * Comparar Budget.montoPlaneado contra la suma real de Transaction de esa
 * categoría en ese periodo es lo que da el "planeado vs real".
 */
public class Budget {

    private final UUID id;
    private final int mes;  // 1-12
    private final int anio;
    private final UUID categoriaId;
    private BigDecimal montoPlaneado;

    public Budget(UUID id, int mes, int anio, UUID categoriaId, BigDecimal montoPlaneado) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes inválido: " + mes);
        }
        if (montoPlaneado == null || montoPlaneado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto planeado no puede ser negativo");
        }
        this.id = id;
        this.mes = mes;
        this.anio = anio;
        this.categoriaId = categoriaId;
        this.montoPlaneado = montoPlaneado;
    }

    public static Budget crearNuevo(int mes, int anio, UUID categoriaId, BigDecimal montoPlaneado) {
        return new Budget(UUID.randomUUID(), mes, anio, categoriaId, montoPlaneado);
    }

    public void ajustarMonto(BigDecimal nuevoMonto) {
        if (nuevoMonto == null || nuevoMonto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto planeado no puede ser negativo");
        }
        this.montoPlaneado = nuevoMonto;
    }

    public UUID getId() { return id; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
    public UUID getCategoriaId() { return categoriaId; }
    public BigDecimal getMontoPlaneado() { return montoPlaneado; }
}