package com.confi.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTypeJpa tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "limite_credito", precision = 19, scale = 2)
    private BigDecimal limiteCredito;

    @Column(name = "dia_corte")
    private Integer diaCorte;

    @Column(name = "dia_pago")
    private Integer diaPago;

    @Column(nullable = false)
    private boolean activa;

    protected AccountEntity() {
        // requerido por JPA
    }

    public AccountEntity(UUID id, String nombre, AccountTypeJpa tipo, BigDecimal saldo,
                          BigDecimal limiteCredito, Integer diaCorte, Integer diaPago, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.saldo = saldo;
        this.limiteCredito = limiteCredito;
        this.diaCorte = diaCorte;
        this.diaPago = diaPago;
        this.activa = activa;
    }

    public enum AccountTypeJpa { DEBITO, CREDITO }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public AccountTypeJpa getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public Integer getDiaCorte() { return diaCorte; }
    public Integer getDiaPago() { return diaPago; }
    public boolean isActiva() { return activa; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public void setActiva(boolean activa) { this.activa = activa; }
}