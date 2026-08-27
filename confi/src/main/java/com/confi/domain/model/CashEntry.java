package com.confi.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CashEntry {

    public enum Movimiento { CARGO, ABONO }

    private final UUID id;
    private final Instant fecha;
    private final Movimiento movimiento;
    private final BigDecimal monto;
    private final String nota;
    private final UUID categoriaId;
    private final String contraparte;
    private final boolean impactaSaldo;

    public CashEntry(UUID id, Instant fecha, Movimiento movimiento, BigDecimal monto,
                     String nota, UUID categoriaId, String contraparte, boolean impactaSaldo) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento es obligatorio");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        this.id = id;
        this.fecha = fecha;
        this.movimiento = movimiento;
        this.monto = monto.setScale(2, java.math.RoundingMode.HALF_UP);
        this.nota = nota;
        this.categoriaId = categoriaId;
        this.contraparte = contraparte;
        this.impactaSaldo = impactaSaldo;
    }

    public static CashEntry crearInformativo(Movimiento movimiento, BigDecimal monto,
                                             String nota, UUID categoriaId, String contraparte,
                                             Instant fecha) {
        Instant fechaFinal = fecha == null ? Instant.now() : fecha;
        return new CashEntry(UUID.randomUUID(), fechaFinal, movimiento, monto,
                nota, categoriaId, contraparte, false);
    }

    public UUID getId() { return id; }
    public Instant getFecha() { return fecha; }
    public Movimiento getMovimiento() { return movimiento; }
    public BigDecimal getMonto() { return monto; }
    public String getNota() { return nota; }
    public UUID getCategoriaId() { return categoriaId; }
    public String getContraparte() { return contraparte; }
    public boolean isImpactaSaldo() { return impactaSaldo; }
}
