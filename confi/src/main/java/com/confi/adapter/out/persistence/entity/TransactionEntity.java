package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    private String nota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeJpa tipo;

    @Column(name = "cuenta_origen_id", nullable = false)
    private UUID cuentaOrigenId;

    @Column(name = "cuenta_destino_id")
    private UUID cuentaDestinoId;

    @Column(name = "categoria_id")
    private UUID categoriaId;

    private String contraparte;

    @Column(name = "subscripcion_id")
    private UUID subscripcionId;

    protected TransactionEntity() {
    }

    public TransactionEntity(UUID id, Instant fecha, BigDecimal monto, String nota, TransactionTypeJpa tipo,
                              UUID cuentaOrigenId, UUID cuentaDestinoId, UUID categoriaId,
                              String contraparte, UUID subscripcionId) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.nota = nota;
        this.tipo = tipo;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.categoriaId = categoriaId;
        this.contraparte = contraparte;
        this.subscripcionId = subscripcionId;
    }

    public enum TransactionTypeJpa { GASTO, INGRESO, TRANSFERENCIA }

    public UUID getId() { return id; }
    public Instant getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public String getNota() { return nota; }
    public TransactionTypeJpa getTipo() { return tipo; }
    public UUID getCuentaOrigenId() { return cuentaOrigenId; }
    public UUID getCuentaDestinoId() { return cuentaDestinoId; }
    public UUID getCategoriaId() { return categoriaId; }
    public String getContraparte() { return contraparte; }
    public UUID getSubscripcionId() { return subscripcionId; }
}