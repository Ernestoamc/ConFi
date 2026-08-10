package com.confi.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Nested
    class Creacion {

        @Test
        void creaUnaCuentaDeDebitoValida() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("1000.00"), null, null, null);

            assertThat(cuenta.getNombre()).isEqualTo("BBVA Debito");
            assertThat(cuenta.getSaldo()).isEqualByComparingTo("1000.00");
            assertThat(cuenta.isActiva()).isTrue();
        }

        @Test
        void rechazaNombreVacio() {
            assertThatThrownBy(() -> Account.crearNueva("", AccountType.DEBITO,
                    BigDecimal.ZERO, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        void unaCuentaDeCreditoRequiereLimiteDeCredito() {
            assertThatThrownBy(() -> Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    BigDecimal.ZERO, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("límite de crédito");
        }

        @Test
        void unaCuentaDeDebitoNoPuedeTenerDiaDeCorteNiPago() {
            assertThatThrownBy(() -> Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    BigDecimal.ZERO, null, 15, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("débito");
        }

        @Test
        void rechazaDiaDeCorteFueraDeRango() {
            assertThatThrownBy(() -> Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    BigDecimal.ZERO, new BigDecimal("10000"), 32, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Día de corte");
        }

        @Test
        void redondeaElSaldoInicialADosDecimales() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("1000.999"), null, null, null);

            assertThat(cuenta.getSaldo()).isEqualByComparingTo("1001.00");
        }
    }

    @Nested
    class MovimientosEnCuentaDeDebito {

        @Test
        void unGastoRestaDelSaldo() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("1000.00"), null, null, null);

            cuenta.aplicarMovimiento(new BigDecimal("-350.50"));

            assertThat(cuenta.getSaldo()).isEqualByComparingTo("649.50");
        }

        @Test
        void unIngresoSumaAlSaldo() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("1000.00"), null, null, null);

            cuenta.aplicarMovimiento(new BigDecimal("500.00"));

            assertThat(cuenta.getSaldo()).isEqualByComparingTo("1500.00");
        }

        @Test
        void noPermiteQuedarEnNegativo() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("100.00"), null, null, null);

            assertThatThrownBy(() -> cuenta.aplicarMovimiento(new BigDecimal("-100.01")))
                    .isInstanceOf(SaldoInsuficienteException.class);
        }

        @Test
        void permiteQuedarExactamenteEnCero() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    new BigDecimal("100.00"), null, null, null);

            cuenta.aplicarMovimiento(new BigDecimal("-100.00"));

            assertThat(cuenta.getSaldo()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    class MovimientosEnCuentaDeCredito {

        @Test
        void unGastoAumentaLaDeuda() {
            Account tarjeta = Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    BigDecimal.ZERO, new BigDecimal("10000.00"), 20, 5);

            tarjeta.aplicarMovimiento(new BigDecimal("-3000.00"));

            assertThat(tarjeta.getSaldo()).isEqualByComparingTo("-3000.00");
        }

        @Test
        void unPagoReduceLaDeuda() {
            Account tarjeta = Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    new BigDecimal("-3000.00"), new BigDecimal("10000.00"), 20, 5);

            tarjeta.aplicarMovimiento(new BigDecimal("1000.00")); // pago desde la cuenta de débito

            assertThat(tarjeta.getSaldo()).isEqualByComparingTo("-2000.00");
        }

        @Test
        void noPermiteExcederElLimiteDeCredito() {
            Account tarjeta = Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    BigDecimal.ZERO, new BigDecimal("10000.00"), 20, 5);

            assertThatThrownBy(() -> tarjeta.aplicarMovimiento(new BigDecimal("-10000.01")))
                    .isInstanceOf(SaldoInsuficienteException.class)
                    .hasMessageContaining("límite de crédito");
        }

        @Test
        void permiteLlegarExactamenteAlLimite() {
            Account tarjeta = Account.crearNueva("Banamex Joy", AccountType.CREDITO,
                    BigDecimal.ZERO, new BigDecimal("10000.00"), 20, 5);

            tarjeta.aplicarMovimiento(new BigDecimal("-10000.00"));

            assertThat(tarjeta.getSaldo()).isEqualByComparingTo("-10000.00");
        }
    }

    @Nested
    class EstadoActivaInactiva {

        @Test
        void unaCuentaNuevaEstaActivaPorDefecto() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    BigDecimal.ZERO, null, null, null);

            assertThat(cuenta.isActiva()).isTrue();
        }

        @Test
        void desactivarCambiaElEstado() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    BigDecimal.ZERO, null, null, null);

            cuenta.desactivar();

            assertThat(cuenta.isActiva()).isFalse();
        }

        @Test
        void reactivarVuelveAActivarla() {
            Account cuenta = Account.crearNueva("BBVA Debito", AccountType.DEBITO,
                    BigDecimal.ZERO, null, null, null);
            cuenta.desactivar();

            cuenta.reactivar();

            assertThat(cuenta.isActiva()).isTrue();
        }
    }
}