package com.confi.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Subscription {

    public enum Frecuencia { MENSUAL, BIMESTRAL, ANUAL }

    private final UUID id;
    private String nombre;
    private BigDecimal montoEstimado;
    private final Frecuencia frecuencia;
    private int diaCobro; // 1-31
    private final UUID cuentaId;
    private final UUID categoriaId;
    private boolean activa;

    public Subscription(UUID id, String nombre, BigDecimal montoEstimado, Frecuencia frecuencia,
                         int diaCobro, UUID cuentaId, UUID categoriaId, boolean activa) {
        if (diaCobro < 1 || diaCobro > 31) {
            throw new IllegalArgumentException("Día de cobro inválido: " + diaCobro);
        }
        this.id = id;
        this.nombre = nombre;
        this.montoEstimado = montoEstimado;
        this.frecuencia = frecuencia;
        this.diaCobro = diaCobro;
        this.cuentaId = cuentaId;
        this.categoriaId = categoriaId;
        this.activa = activa;
    }

    public static Subscription crearNueva(String nombre, BigDecimal montoEstimado, Frecuencia frecuencia,
                                           int diaCobro, UUID cuentaId, UUID categoriaId) {
        return new Subscription(UUID.randomUUID(), nombre, montoEstimado, frecuencia, diaCobro,
                cuentaId, categoriaId, true);
    }

    public void pausar() { this.activa = false; }
    public void reactivar() { this.activa = true; }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getMontoEstimado() { return montoEstimado; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public int getDiaCobro() { return diaCobro; }
    public UUID getCuentaId() { return cuentaId; }
    public UUID getCategoriaId() { return categoriaId; }
    public boolean isActiva() { return activa; }
}