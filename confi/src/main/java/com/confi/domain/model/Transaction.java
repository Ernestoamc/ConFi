package com.confi.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa un movimiento de dinero: gasto, ingreso o transferencia.
 *
 * Casos que cubre:
 *  - GASTO: sale dinero de cuentaOrigenId, se clasifica con categoriaId.
 *  - INGRESO: entra dinero a cuentaOrigenId (aquí actúa como "cuenta destino"), con categoriaId.
 *  - TRANSFERENCIA entre cuentas propias: cuentaOrigenId y cuentaDestinoId ambos presentes.
 *  - TRANSFERENCIA a/de terceros: cuentaDestinoId es null, se usa contraparte (nombre de la persona).
 */
public class Transaction {

    private final UUID id;
    private final Instant fecha;
    private final BigDecimal monto; // siempre positivo, el signo lo decide el tipo
    private final String nota;
    private final TransactionType tipo;
    private final UUID cuentaOrigenId;
    private final UUID cuentaDestinoId; // solo TRANSFERENCIA entre cuentas propias
    private final UUID categoriaId;     // solo GASTO / INGRESO
    private final String contraparte;   // solo TRANSFERENCIA a terceros
    private final UUID subscripcionId;  // opcional, referencia a un pago recurrente

    private Transaction(UUID id, Instant fecha, BigDecimal monto, String nota, TransactionType tipo,
                         UUID cuentaOrigenId, UUID cuentaDestinoId, UUID categoriaId,
                         String contraparte, UUID subscripcionId) {
        validar(monto, tipo, cuentaOrigenId, cuentaDestinoId, categoriaId, contraparte);
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

    private static void validar(BigDecimal monto, TransactionType tipo, UUID cuentaOrigenId,
                                 UUID cuentaDestinoId, UUID categoriaId, String contraparte) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (cuentaOrigenId == null) {
            throw new IllegalArgumentException("La cuenta origen es obligatoria");
        }
        if (tipo == TransactionType.TRANSFERENCIA) {
            boolean esEntreCuentasPropias = cuentaDestinoId != null;
            boolean esAContraparte = contraparte != null && !contraparte.isBlank();
            if (esEntreCuentasPropias == esAContraparte) {
                throw new IllegalArgumentException(
                        "Una transferencia debe tener cuentaDestinoId (propia) O contraparte (tercero), no ambos ni ninguno");
            }
            if (categoriaId != null) {
                throw new IllegalArgumentException("Una transferencia no lleva categoría");
            }
        } else {
            if (categoriaId == null) {
                throw new IllegalArgumentException("Un " + tipo + " requiere categoría");
            }
            if (cuentaDestinoId != null || (contraparte != null && !contraparte.isBlank())) {
                throw new IllegalArgumentException("Un " + tipo + " no lleva cuenta destino ni contraparte");
            }
        }
    }

    public static Transaction gasto(BigDecimal monto, String nota, UUID cuentaOrigenId,
                                     UUID categoriaId, Instant fecha) {
        return new Transaction(UUID.randomUUID(), fecha, monto, nota, TransactionType.GASTO,
                cuentaOrigenId, null, categoriaId, null, null);
    }

    public static Transaction ingreso(BigDecimal monto, String nota, UUID cuentaDestinoId,
                                       UUID categoriaId, Instant fecha) {
        return new Transaction(UUID.randomUUID(), fecha, monto, nota, TransactionType.INGRESO,
                cuentaDestinoId, null, categoriaId, null, null);
    }

    public static Transaction transferenciaEntreCuentasPropias(BigDecimal monto, String nota,
                                                                 UUID cuentaOrigenId, UUID cuentaDestinoId,
                                                                 Instant fecha) {
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser la misma");
        }
        return new Transaction(UUID.randomUUID(), fecha, monto, nota, TransactionType.TRANSFERENCIA,
                cuentaOrigenId, cuentaDestinoId, null, null, null);
    }

    public static Transaction transferenciaATercero(BigDecimal monto, String nota, UUID cuentaOrigenId,
                                                      String contraparte, Instant fecha) {
        return new Transaction(UUID.randomUUID(), fecha, monto, nota, TransactionType.TRANSFERENCIA,
                cuentaOrigenId, null, null, contraparte, null);
    }
    public static Transaction reconstruir(UUID id, Instant fecha, BigDecimal monto, String nota,
                                       TransactionType tipo, UUID cuentaOrigenId, UUID cuentaDestinoId,
                                       UUID categoriaId, String contraparte, UUID subscripcionId) {
    return new Transaction(id, fecha, monto, nota, tipo, cuentaOrigenId, cuentaDestinoId,
            categoriaId, contraparte, subscripcionId);
}

    /** true si esta transferencia mueve dinero entre dos cuentas propias del usuario */
    public boolean esTransferenciaInterna() {
        return tipo == TransactionType.TRANSFERENCIA && cuentaDestinoId != null;
    }

    public UUID getId() { return id; }
    public Instant getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public String getNota() { return nota; }
    public TransactionType getTipo() { return tipo; }
    public UUID getCuentaOrigenId() { return cuentaOrigenId; }
    public UUID getCuentaDestinoId() { return cuentaDestinoId; }
    public UUID getCategoriaId() { return categoriaId; }
    public String getContraparte() { return contraparte; }
    public UUID getSubscripcionId() { return subscripcionId; }
}