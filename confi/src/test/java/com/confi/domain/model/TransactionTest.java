package com.confi.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private final UUID cuentaOrigen = UUID.randomUUID();
    private final UUID cuentaDestino = UUID.randomUUID();
    private final UUID categoria = UUID.randomUUID();
    private final Instant ahora = Instant.now();

    @Nested
    class Gasto {

        @Test
        void creaUnGastoValido() {
            Transaction tx = Transaction.gasto(new BigDecimal("350.50"), "Cena",
                    cuentaOrigen, categoria, null, null, ahora);

            assertThat(tx.getTipo()).isEqualTo(TransactionType.GASTO);
            assertThat(tx.getMonto()).isEqualByComparingTo("350.50");
            assertThat(tx.getCategoriaId()).isEqualTo(categoria);
        }

        @Test
        void unGastoPuedeTenerContraparteOpcional() {
            Transaction tx = Transaction.gasto(new BigDecimal("500.00"), "Le presté a Juan",
                    cuentaOrigen, categoria, "Juan", null, ahora);

            assertThat(tx.getContraparte()).isEqualTo("Juan");
        }

        @Test
        void unGastoRequiereCategoria() {
            assertThatThrownBy(() -> Transaction.gasto(new BigDecimal("100"), "Cena",
                    cuentaOrigen, null, null, null, ahora))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("categoría");
        }

        @Test
        void rechazaMontoCeroONegativo() {
            assertThatThrownBy(() -> Transaction.gasto(BigDecimal.ZERO, "Cena",
                    cuentaOrigen, categoria, null, null, ahora))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("monto");
        }
    }

    @Nested
    class Ingreso {

        @Test
        void unIngresoPuedeTenerContraparteOpcional() {
            Transaction tx = Transaction.ingreso(new BigDecimal("500.00"), "Juan me pagó",
                    cuentaOrigen, categoria, "Juan", ahora);

            assertThat(tx.getTipo()).isEqualTo(TransactionType.INGRESO);
            assertThat(tx.getContraparte()).isEqualTo("Juan");
        }
    }

    @Nested
    class TransferenciaEntreCuentasPropias {

        @Test
        void creaUnaTransferenciaValida() {
            Transaction tx = Transaction.transferenciaEntreCuentasPropias(
                    new BigDecimal("1000.00"), "Pago de tarjeta", cuentaOrigen, cuentaDestino, ahora);

            assertThat(tx.esTransferenciaInterna()).isTrue();
            assertThat(tx.getCuentaDestinoId()).isEqualTo(cuentaDestino);
            assertThat(tx.getCategoriaId()).isNull();
        }

        @Test
        void rechazaCuentaOrigenIgualADestino() {
            assertThatThrownBy(() -> Transaction.transferenciaEntreCuentasPropias(
                    new BigDecimal("100"), "x", cuentaOrigen, cuentaOrigen, ahora))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("misma");
        }
    }

    @Nested
    class TransferenciaATercero {

        @Test
        void creaUnaTransferenciaATerceroValida() {
            Transaction tx = Transaction.transferenciaATercero(
                    new BigDecimal("800.00"), "Renta", cuentaOrigen, "Casero", ahora);

            assertThat(tx.esTransferenciaInterna()).isFalse();
            assertThat(tx.getContraparte()).isEqualTo("Casero");
            assertThat(tx.getCuentaDestinoId()).isNull();
        }
    }

    @Nested
    class ValidacionDeTransferencias {

        @Test
        void noPermiteCuentaDestinoYContraparteALaVez() {
            assertThatThrownBy(() -> {
                // construido manualmente vía reconstruir para forzar el estado inválido
                // (los factories públicos ya lo impiden, esto prueba la validación interna)
                Transaction.reconstruir(UUID.randomUUID(), ahora, new BigDecimal("100"), "x",
                        TransactionType.TRANSFERENCIA, cuentaOrigen, cuentaDestino, null, "Juan", null,
                        null, null);
            }).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no ambos ni ninguno");
        }

        @Test
        void noPermiteNiCuentaDestinoNiContraparte() {
            assertThatThrownBy(() -> Transaction.reconstruir(UUID.randomUUID(), ahora,
                    new BigDecimal("100"), "x", TransactionType.TRANSFERENCIA,
                    cuentaOrigen, null, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no ambos ni ninguno");
        }

        @Test
        void unaTransferenciaNoLlevaCategoria() {
            assertThatThrownBy(() -> Transaction.reconstruir(UUID.randomUUID(), ahora,
                    new BigDecimal("100"), "x", TransactionType.TRANSFERENCIA,
                    cuentaOrigen, cuentaDestino, categoria, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("categoría");
        }
    }

    @Nested
    class SaldosResultantes {

        @Test
        void seRegistranUnaSolaVez() {
            Transaction tx = Transaction.gasto(new BigDecimal("100"), "Cena",
                    cuentaOrigen, categoria, null, null, ahora);

            tx.registrarSaldosResultantes(new BigDecimal("900.00"), null);

            assertThat(tx.getSaldoOrigenDespues()).isEqualByComparingTo("900.00");
            assertThatThrownBy(() -> tx.registrarSaldosResultantes(new BigDecimal("800.00"), null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class Reconstruccion {

        @Test
        void preservaElIdOriginalYLosSaldosHistoricos() {
            UUID idOriginal = UUID.randomUUID();

            Transaction tx = Transaction.reconstruir(idOriginal, ahora, new BigDecimal("100"), "Cena",
                    TransactionType.GASTO, cuentaOrigen, null, categoria, null, null,
                    new BigDecimal("900.00"), null);

            assertThat(tx.getId()).isEqualTo(idOriginal);
            assertThat(tx.getSaldoOrigenDespues()).isEqualByComparingTo("900.00");
        }
    }
}