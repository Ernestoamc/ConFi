package com.confi.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representa una cuenta bancaria del usuario (débito o crédito).
 */
public class Account {

    private final UUID id;
    private String nombre;
    private final AccountType tipo;
    private BigDecimal saldo;
    private final BigDecimal limiteCredito; 
    private final Integer diaCorte;         
    private final Integer diaPago;          
    private boolean activa;

    public Account(UUID id, String nombre, AccountType tipo, BigDecimal saldo,
                    BigDecimal limiteCredito, Integer diaCorte, Integer diaPago, boolean activa) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la cuenta no puede estar vacío");
        }
        if (tipo == AccountType.CREDITO && limiteCredito == null) {
            throw new IllegalArgumentException("Una cuenta de crédito requiere límite de crédito");
        }
        if (tipo == AccountType.DEBITO && (diaCorte != null || diaPago != null)) {
            throw new IllegalArgumentException("Una cuenta de débito no tiene día de corte ni de pago");
        }
        validarDia(diaCorte, "corte");
        validarDia(diaPago, "pago");
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = saldo.setScale(2, java.math.RoundingMode.HALF_UP);
        this.limiteCredito = limiteCredito == null ? null : limiteCredito.setScale(2, java.math.RoundingMode.HALF_UP);
        this.diaCorte = diaCorte;
        this.diaPago = diaPago;
        this.activa = activa;
    }

    private static void validarDia(Integer dia, String etiqueta) {
        if (dia != null && (dia < 1 || dia > 31)) {
            throw new IllegalArgumentException("Día de " + etiqueta + " inválido: " + dia);
        }
    }

    public static Account crearNueva(String nombre, AccountType tipo, BigDecimal saldoInicial,
                                      BigDecimal limiteCredito, Integer diaCorte, Integer diaPago) {
        return new Account(UUID.randomUUID(), nombre, tipo, saldoInicial, limiteCredito, diaCorte, diaPago, true);
    }

    /**
     * Aplica un movimiento de dinero a la cuenta.
     * Positivo = entra dinero (ingreso o le transfieren).
     * Negativo = sale dinero (gasto o transferencia enviada).
     *
     * En cuentas de crédito, un gasto (negativo) aumenta la deuda,
     * lo cual se modela simplemente restando del saldo (saldo puede quedar negativo).
     */
    public void aplicarMovimiento(BigDecimal monto) {
        BigDecimal nuevoSaldo = this.saldo.add(monto).setScale(2, java.math.RoundingMode.HALF_UP);

        if (tipo == AccountType.CREDITO) {
            BigDecimal deudaMaxima = limiteCredito.negate();
            if (nuevoSaldo.compareTo(deudaMaxima) < 0) {
                throw new SaldoInsuficienteException(
                        "El movimiento excede el límite de crédito de la cuenta " + nombre);
            }
        } else {
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new SaldoInsuficienteException(
                        "Saldo insuficiente en la cuenta " + nombre);
            }
        }

        this.saldo = nuevoSaldo;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public AccountType getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public Integer getDiaCorte() { return diaCorte; }
    public Integer getDiaPago() { return diaPago; }
    public boolean isActiva() { return activa; }

    public void renombrar(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nuevoNombre;
    }

    public void desactivar() {
        this.activa = false;
    }

    public void reactivar() {
        this.activa = true;
    }
}