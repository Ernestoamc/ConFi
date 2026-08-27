package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cash_transactions")
public class CashEntryEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovimientoJpa movimiento;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    private String nota;

    @Column(name = "categoria_id")
    private UUID categoriaId;

    private String contraparte;

    @Column(name = "impacta_saldo", nullable = false)
    private boolean impactaSaldo;

    protected CashEntryEntity() {
    }

    public CashEntryEntity(UUID id, Instant fecha, MovimientoJpa movimiento, BigDecimal monto,
                           String nota, UUID categoriaId, String contraparte, boolean impactaSaldo) {
        this.id = id;
        this.fecha = fecha;
        this.movimiento = movimiento;
        this.monto = monto;
        this.nota = nota;
        this.categoriaId = categoriaId;
        this.contraparte = contraparte;
        this.impactaSaldo = impactaSaldo;
    }

    public enum MovimientoJpa { CARGO, ABONO }

    public UUID getId() { return id; }
    public Instant getFecha() { return fecha; }
    public MovimientoJpa getMovimiento() { return movimiento; }
    public BigDecimal getMonto() { return monto; }
    public String getNota() { return nota; }
    public UUID getCategoriaId() { return categoriaId; }
    public String getContraparte() { return contraparte; }
    public boolean isImpactaSaldo() { return impactaSaldo; }
}
