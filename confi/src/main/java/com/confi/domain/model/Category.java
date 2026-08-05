package com.confi.domain.model;

import java.util.UUID;

public class Category {

    private final UUID id;
    private String nombre;
    private final TransactionType tipo; // GASTO o INGRESO (las categorías no aplican a TRANSFERENCIA)
    private String icono;
    private boolean esSubscripcion;

    public Category(UUID id, String nombre, TransactionType tipo, String icono, boolean esSubscripcion) {
        if (tipo == TransactionType.TRANSFERENCIA) {
            throw new IllegalArgumentException("Una categoría no puede ser de tipo TRANSFERENCIA");
        }
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.icono = icono;
        this.esSubscripcion = esSubscripcion;
    }

    public static Category crearNueva(String nombre, TransactionType tipo, String icono, boolean esSubscripcion) {
        return new Category(UUID.randomUUID(), nombre, tipo, icono, esSubscripcion);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public TransactionType getTipo() { return tipo; }
    public String getIcono() { return icono; }
    public boolean isEsSubscripcion() { return esSubscripcion; }
}