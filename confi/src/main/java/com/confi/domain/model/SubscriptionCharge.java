package com.confi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public class SubscriptionCharge{
    public enum Estado { PENDIENTE, CONFIRMADO, OMITIDO }
    private final UUID id;
    private final UUID subscripcionId;
    private final LocalDate fechaEsperada;
    private final BigDecimal montoEsperado;
    private Estado estado;
    private UUID transactionId;

    public SubscriptionCharge(UUID id, UUID subscripcionId, LocalDate fechaEsperada,
        BigDecimal montoEsperado, Estado estado, UUID transactionID){
            if(subscripcionId == null){
                throw new IllegalArgumentException("subscripcionId es obligatorio");
            }
            if(fechaEsperada == null){
                throw new IllegalArgumentException("fechaEsperada es obligatoria");
            }
            if(montoEsperado == null){
                throw new IllegalArgumentException("montoEsperado debe ser mayor a cero");
            }
            this.id = id;
            this.subscripcionId = subscripcionId;
            this.fechaEsperada = fechaEsperada;
            this.montoEsperado = montoEsperado.setScale(2, java.math.RoundingMode.HALF_UP);
            this.estado = estado;
            this.transactionId = transactionId;
        }

        public static SubscriptionCharge crearPendiente(UUID subscripcionId, LocalDate fechaEsperada, BigDecimal montoEsperado){
            return new SubscriptionCharge(UUID.randomUUID(), subscripcionId, fechaEsperada, montoEsperado, Estado.PENDIENTE, null);
        }        

        public void confirmar(UUID transactionId){
            if(estado != Estado.PENDIENTE) {
                throw new IllegalStateException("Solo un cargo PENDIENTE puede confirmarse (cuando el actual: " + estado + ")");
            }
            if(transactionId == null){
                throw new IllegalArgumentException("transactionid es obligatorio al confirmar");
            }
            this.estado = Estado.CONFIRMADO;
            this.transactionId = transactionId;
        }

        public void omitir(){
            if(estado != Estado.PENDIENTE){
                throw new IllegalStateException("Solo un cargo PENDIENTE puede omitirse (estado actual: " + estado + ")");
            }
            this.estado = Estado.OMITIDO;
        }
        public UUID getId(){return id;}
        public UUID getSubscripcionId() { return subscripcionId; }        
        public LocalDate getFechaEsperada(){return fechaEsperada;}
        public BigDecimal getMontoEsperado(){return montoEsperado;}
        public Estado getEstado(){return estado;}
        public UUID getTransactionId(){return transactionId;}
        
}