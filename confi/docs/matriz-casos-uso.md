# Matriz de Casos de Uso y Alcance Funcional

Este documento describe todo lo que hoy puedes hacer por API en ConFi, con foco en cuentas debito/credito, consultas, transacciones, traspasos, transferencias y suscripciones.

## Convenciones

- Base URL local: `http://localhost:8080`
- Formato de error estandar: `timestamp`, `status`, `error`, `mensaje` y en validaciones `detalles`
- Codigos frecuentes:
  - `201` creado
  - `200` exito
  - `400` validacion/regla invalida
  - `404` recurso no encontrado
  - `409` conflicto de datos
  - `422` saldo insuficiente

## 1) Mapa Completo de Capacidades

### 1.1 Cuentas (debito y credito)

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Crear cuenta debito | Disponible | `POST /api/accounts` |
| Crear cuenta credito | Disponible | `POST /api/accounts` |
| Consultar todas las cuentas activas | Disponible | `GET /api/accounts` |
| Consultar cuenta individual por id | Disponible | `GET /api/accounts/{id}` |
| Editar nombre de cuenta | No expuesto por API | N/A |
| Desactivar cuenta | No expuesto por API | N/A |
| Reactivar cuenta | No expuesto por API | N/A |

### 1.2 Movimientos de dinero

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Registrar gasto | Disponible | `POST /api/transactions` |
| Registrar ingreso | Disponible | `POST /api/transactions` |
| Traspaso entre cuentas propias | Disponible (`tipo=TRANSFERENCIA` + `cuentaDestinoId`) | `POST /api/transactions` |
| Transferencia a tercero | Disponible (`tipo=TRANSFERENCIA` + `contraparte`) | `POST /api/transactions` |
| Recibir dinero de terceros | Disponible (se registra como `INGRESO` con `contraparte`) | `POST /api/transactions` |
| Registrar retiro de efectivo desde cuenta bancaria | No expuesto por API como caso dedicado (se puede registrar como gasto) | N/A |
| Registrar gasto en efectivo informativo sin afectar saldos bancarios | No expuesto por API | N/A |
| Historial/listado de transacciones | No expuesto por API | N/A |
| Estado de cuenta por cuenta (cargo/abono + saldo acumulado) | No expuesto por API | N/A |
| Estado de cuenta general consolidado | No expuesto por API | N/A |
| Editar transaccion | No expuesto por API | N/A |
| Eliminar transaccion | No expuesto por API | N/A |

### 1.3 Suscripciones y cargos

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Crear suscripcion | Disponible | `POST /api/subscriptions` |
| Listar suscripciones activas | Disponible | `GET /api/subscriptions` |
| Pausar suscripcion | Disponible | `PATCH /api/subscriptions/{id}/pausar` |
| Reactivar suscripcion | No expuesto por API | N/A |
| Generar cargos del mes | Disponible | `POST /api/subscription-charges/generar?mes={mes}&anio={anio}` |
| Listar cargos por periodo | Disponible | `GET /api/subscription-charges?mes={mes}&anio={anio}` |
| Confirmar cargo | Disponible | `POST /api/subscription-charges/{id}/confirmar` |
| Omitir cargo | Disponible | `POST /api/subscription-charges/{id}/omitir` |

### 1.4 Observabilidad

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Health | Disponible | `GET /actuator/health` |
| Info | Disponible | `GET /actuator/info` |

### 1.5 Planeacion y presupuesto

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Presupuesto mensual por categoria | No expuesto por API | N/A |
| Presupuesto quincenal por categoria | No modelado/no expuesto por API | N/A |
| Presupuesto semanal por categoria | No modelado/no expuesto por API | N/A |
| Comparativo planeado vs real por periodo | Parcial en dominio (caso de uso mensual), no expuesto por API | N/A |

### 1.6 Analitica financiera

| Operacion | Estado actual API | Endpoint |
|---|---|---|
| Estado de resultados por cuenta (ingresos - gastos) | No expuesto por API | N/A |
| Estado de resultados general consolidado | Parcial en dominio (caso de uso mensual), no expuesto por API | N/A |

## 2) Reglas Clave de Cuentas Debito y Credito

| Tema | Debito | Credito |
|---|---|---|
| Limite de credito | No aplica | Obligatorio |
| Dia de corte / pago | No permitido | Permitido (1..31) |
| Saldo negativo | No permitido | Permitido hasta `-limiteCredito` |
| Exceso de limite/saldo | Responde `422` por saldo insuficiente | Responde `422` por exceder limite |

## 3) Casos de Uso Detallados

## 3.1 Cuentas

| ID | Caso | Endpoint | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| ACC-01 | Crear cuenta debito valida | /api/accounts | POST | Ninguna | `201` y cuenta activa creada |
| ACC-02 | Crear cuenta credito valida | /api/accounts | POST | Ninguna | `201` y cuenta con limite de credito |
| ACC-03 | Crear credito sin limite | /api/accounts | POST | Ninguna | `400` |
| ACC-04 | Crear debito con diaCorte/diaPago | /api/accounts | POST | Ninguna | `400` |
| ACC-05 | Crear cuenta con datos invalidos | /api/accounts | POST | Ninguna | `400` con `detalles` |
| ACC-06 | Listar cuentas activas | /api/accounts | GET | Cuentas existentes o no | `200` con lista |
| ACC-07 | Obtener cuenta por id existente | /api/accounts/{id} | GET | Cuenta creada | `200` con cuenta |
| ACC-08 | Obtener cuenta por id inexistente | /api/accounts/{id} | GET | Id no existente | `404` |

### Request ejemplo ACC-01

```http
POST /api/accounts
Content-Type: application/json

{
  "nombre": "BBVA Debito",
  "tipo": "DEBITO",
  "saldoInicial": 5000.00,
  "limiteCredito": null,
  "diaCorte": null,
  "diaPago": null
}
```

### Request ejemplo ACC-02

```http
POST /api/accounts
Content-Type: application/json

{
  "nombre": "Tarjeta Oro",
  "tipo": "CREDITO",
  "saldoInicial": 0.00,
  "limiteCredito": 15000.00,
  "diaCorte": 20,
  "diaPago": 5
}
```

## 3.2 Transacciones, Traspasos y Transferencias

| ID | Caso | Endpoint | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| TX-01 | Registrar gasto | /api/transactions | POST | Cuenta origen y categoria validas | `201`, saldo origen disminuye |
| TX-02 | Registrar ingreso | /api/transactions | POST | Cuenta origen y categoria validas | `201`, saldo origen aumenta |
| TX-03 | Traspaso entre cuentas propias | /api/transactions | POST | Cuenta origen y destino validas | `201`, saldos origen/destino actualizados |
| TX-04 | Transferencia a tercero | /api/transactions | POST | Cuenta origen valida y contraparte | `201`, solo saldo origen actualizado |
| TX-05 | Transferencia sin destino ni contraparte | /api/transactions | POST | Ninguna | `400` |
| TX-06 | Transferencia con destino y contraparte a la vez | /api/transactions | POST | Ninguna | `400` |
| TX-07 | Monto no valido o campos faltantes | /api/transactions | POST | Ninguna | `400` con `detalles` |
| TX-08 | Saldo insuficiente / limite excedido | /api/transactions | POST | Cuenta sin saldo/límite suficiente | `422` |
| TX-09 | Cuenta origen o destino inexistente | /api/transactions | POST | Id inexistente | `404` |

### Request ejemplo TX-01 (gasto)

```http
POST /api/transactions
Content-Type: application/json

{
  "tipo": "GASTO",
  "monto": 350.50,
  "nota": "Cena",
  "cuentaOrigenId": "<uuid>",
  "categoriaId": "<uuid>"
}
```

### Request ejemplo TX-03 (traspaso interno)

```http
POST /api/transactions
Content-Type: application/json

{
  "tipo": "TRANSFERENCIA",
  "monto": 1000.00,
  "nota": "Pago de tarjeta",
  "cuentaOrigenId": "<uuid>",
  "cuentaDestinoId": "<uuid>"
}
```

### Request ejemplo TX-04 (transferencia a tercero)

```http
POST /api/transactions
Content-Type: application/json

{
  "tipo": "TRANSFERENCIA",
  "monto": 800.00,
  "nota": "Renta",
  "cuentaOrigenId": "<uuid>",
  "contraparte": "Casero"
}
```

### Request ejemplo TX-10 (recibir nomina o deposito de tercero)

```http
POST /api/transactions
Content-Type: application/json

{
  "tipo": "INGRESO",
  "monto": 1000.00,
  "nota": "Nomina quincenal",
  "cuentaOrigenId": "<uuid>",
  "categoriaId": "<uuid>",
  "contraparte": "Empresa ACME"
}
```

## 3.3 Suscripciones

| ID | Caso | Endpoint | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| SUB-01 | Crear suscripcion valida | /api/subscriptions | POST | Cuenta y categoria validas | `201` |
| SUB-02 | Crear con diaCobro fuera de rango | /api/subscriptions | POST | Ninguna | `400` con `detalles` |
| SUB-03 | Crear con monto no positivo | /api/subscriptions | POST | Ninguna | `400` |
| SUB-04 | Listar suscripciones activas | /api/subscriptions | GET | Ninguna | `200` |
| SUB-05 | Pausar suscripcion existente | /api/subscriptions/{id}/pausar | PATCH | Suscripcion existente | `200` (sin body) |
| SUB-06 | Pausar suscripcion inexistente | /api/subscriptions/{id}/pausar | PATCH | Id inexistente | `404` |

### Request ejemplo SUB-01

```http
POST /api/subscriptions
Content-Type: application/json

{
  "nombre": "Netflix",
  "montoEstimado": 249.00,
  "frecuencia": "MENSUAL",
  "diaCobro": 15,
  "cuentaId": "<uuid>",
  "categoriaId": "<uuid>"
}
```

## 3.4 Cargos de Suscripcion

| ID | Caso | Endpoint | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| CHG-01 | Generar cargos del mes | /api/subscription-charges/generar?mes={mes}&anio={anio} | POST | Suscripciones activas | `200` con lista de cargos generados |
| CHG-02 | Generar con mes/anio invalidos | /api/subscription-charges/generar | POST | Ninguna | `400` |
| CHG-03 | Listar cargos del mes | /api/subscription-charges?mes={mes}&anio={anio} | GET | Ninguna | `200` |
| CHG-04 | Confirmar cargo pendiente | /api/subscription-charges/{id}/confirmar | POST | Cargo pendiente existente | `200`, estado `CONFIRMADO` |
| CHG-05 | Omitir cargo pendiente | /api/subscription-charges/{id}/omitir | POST | Cargo pendiente existente | `200`, estado `OMITIDO` |
| CHG-06 | Confirmar/omitir id inexistente | /api/subscription-charges/{id}/confirmar u omitir | POST | Id inexistente | `404` |
| CHG-07 | Confirmar cargo ya confirmado/omitido | /api/subscription-charges/{id}/confirmar | POST | Cargo no pendiente | `400` |

### Request ejemplo CHG-01

```http
POST /api/subscription-charges/generar?mes=8&anio=2026
```

## 3.5 Observabilidad

| ID | Caso | Endpoint | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| OBS-01 | Health general | /actuator/health | GET | App levantada | `200` y `status` |
| OBS-02 | Info general | /actuator/info | GET | App levantada | `200` |

## 3.6 Casos de Uso Propuestos (Pendientes)

Estos casos no existen hoy por endpoint, pero quedan definidos para validar implementacion futura.

| ID | Caso | Endpoint propuesto | Metodo | Precondicion | Resultado esperado |
|---|---|---|---|---|---|
| PEND-ACC-01 | Desactivar cuenta | /api/accounts/{id}/desactivar | PATCH | Cuenta existente | `200`, `activa=false` |
| PEND-ACC-02 | Reactivar cuenta | /api/accounts/{id}/reactivar | PATCH | Cuenta existente | `200`, `activa=true` |
| PEND-ACC-03 | Editar nombre/parametros de cuenta | /api/accounts/{id} | PUT/PATCH | Cuenta existente | `200` con cuenta actualizada |
| PEND-TX-01 | Listar transacciones por cuenta y periodo | /api/transactions?cuentaId={id}&desde={f1}&hasta={f2} | GET | Cuenta existente | `200` con movimientos ordenados |
| PEND-TX-02 | Estado de cuenta por cuenta con cargo/abono y saldo acumulado | /api/accounts/{id}/statement?desde={f1}&hasta={f2} | GET | Cuenta con movimientos | `200` con detalle y saldo inicial/final |
| PEND-TX-03 | Estado de cuenta general consolidado | /api/statement?desde={f1}&hasta={f2} | GET | Usuario con multiples cuentas | `200` con consolidado y subtotales por cuenta |
| PEND-TX-04 | Editar transaccion | /api/transactions/{id} | PATCH/PUT | Transaccion existente | `200` con movimiento ajustado y trazabilidad |
| PEND-TX-05 | Eliminar/cancelar transaccion | /api/transactions/{id} | DELETE | Transaccion existente | `200/204` y movimiento reversado o anulado |
| PEND-CASH-01 | Registrar retiro de efectivo (sale de debito/credito) | /api/cash/withdrawals | POST | Cuenta existente con saldo o limite disponible | `201` y afectacion de saldo de cuenta origen |
| PEND-CASH-02 | Registrar gasto en efectivo informativo (no afecta saldos bancarios) | /api/cash/transactions | POST | Ninguna | `201` y solo registro analitico |
| PEND-CASH-03 | Listar flujo de efectivo informativo por periodo | /api/cash/transactions?desde={f1}&hasta={f2} | GET | Ninguna | `200` con cargos/abonos de efectivo |
| PEND-CASH-04 | Incluir o excluir movimientos informativos de efectivo en reportes | /api/reports/*?includeInformativeCash=true|false | GET | Ninguna | `200` con resultado consistente segun bandera |
| PEND-SUB-01 | Reactivar suscripcion pausada | /api/subscriptions/{id}/reactivar | PATCH | Suscripcion pausada | `200` |
| PEND-BUD-01 | Crear presupuesto mensual | /api/budgets | POST | Categoria valida | `201` |
| PEND-BUD-02 | Listar presupuestos por periodo | /api/budgets?mes={mes}&anio={anio} | GET | Ninguna | `200` |
| PEND-BUD-03 | Ajustar presupuesto mensual | /api/budgets/{id} | PATCH | Presupuesto existente | `200` |
| PEND-BUD-04 | Presupuesto quincenal | /api/budgets/biweekly | POST/GET | Definir reglas de quincena | `201/200` |
| PEND-BUD-05 | Presupuesto semanal | /api/budgets/weekly | POST/GET | Definir reglas de semana | `201/200` |
| PEND-BUD-06 | Reporte planeado vs real por mes/quincena/semana | /api/reports/budget-vs-actual | GET | Presupuestos y movimientos cargados | `200` |
| PEND-RES-01 | Estado de resultados por cuenta | /api/reports/income-statement?cuentaId={id}&desde={f1}&hasta={f2} | GET | Cuenta existente y movimientos en periodo | `200` con ingresos, gastos y resultado neto |
| PEND-RES-02 | Estado de resultados general consolidado | /api/reports/income-statement?desde={f1}&hasta={f2} | GET | Movimientos en multiples cuentas | `200` con totales globales y desglose por cuenta |

### Detalle funcional requerido para estado de cuenta (PEND-TX-02 / PEND-TX-03)

El estado de cuenta debe mostrar, por cada movimiento:

1. Fecha y hora.
2. Descripcion/nota.
3. Tipo visible: `CARGO` o `ABONO`.
4. Monto del cargo o abono.
5. Saldo despues de la transaccion (saldo acumulado).

Ejemplo esperado de flujo trazable:

1. Saldo inicial: 0.00
2. ABONO nomina: +1000.00 -> saldo 1000.00
3. CARGO supermercado: -50.00 -> saldo 950.00
4. CARGO transporte: -20.00 -> saldo 930.00

Notas de implementacion sugerida:

1. Reusar `saldoOrigenDespues` y `saldoDestinoDespues` ya guardados en transaccion.
2. Para cuenta individual, ordenar por fecha ascendente y calcular/mostrar saldo inicial del periodo.
3. Para estado general, consolidar por cuenta y luego total global.
4. Mantener trazabilidad de ediciones/cancelaciones con politica auditable.

### Detalle funcional requerido para efectivo (PEND-CASH-01 .. PEND-CASH-04)

Objetivo: permitir registrar efectivo sin romper el control de cuentas bancarias.

Reglas propuestas:

1. Retiro de efectivo desde banco SI afecta el saldo de la cuenta origen.
2. Gasto en efectivo informativo NO afecta saldos de cuentas bancarias.
3. El movimiento informativo debe marcarse con `impactaSaldo=false` y `medioPago=EFECTIVO`.
4. En reportes, poder incluir o excluir efectivo informativo mediante bandera.

Ejemplo de flujo solicitado:

1. Retiro efectivo 1,000.00 desde debito: saldo banco baja 1,000.00.
2. Pago luz en efectivo 500.00: se registra para analitica, pero no vuelve a bajar saldo banco.
3. Estado de cuenta bancario mantiene coherencia.
4. Estado de resultados puede mostrar el gasto en efectivo (si `includeInformativeCash=true`).

### Detalle funcional requerido para estado de resultados (PEND-RES-01 / PEND-RES-02)

El estado de resultados debe mostrar, por periodo:

1. Total ingresos.
2. Total gastos.
3. Resultado neto (`ingresos - gastos`).
4. Desglose por categoria (opcional en v1, recomendado).
5. Desglose por cuenta para vista consolidada general.

Ejemplo esperado:

1. Ingresos: 10,000.00
2. Gastos: 7,300.00
3. Resultado neto: 2,700.00

Notas de implementacion sugerida:

1. Reusar base de `GetMonthlyReportUseCase` para la version mensual.
2. Extender a rangos arbitrarios (`desde`/`hasta`) para quincena y semana.
3. Mantener separado el estado de cuenta (detalle transaccional) del estado de resultados (resumen financiero).
4. En vista por cuenta, excluir transferencias internas del resultado neto o marcarlas sin afectar utilidad personal, segun regla contable definida.

## 4) Alcance Pendiente (no disponible hoy por API)

Estas operaciones existen como necesidad funcional pero no tienen endpoint actualmente.

| Area | Pendiente |
|---|---|
| Cuentas | Editar cuenta, desactivar cuenta, reactivar cuenta |
| Transacciones | Historial por cuenta/periodo, edicion/cancelacion, estado de cuenta por cuenta, estado de cuenta general |
| Efectivo | Retiro de efectivo dedicado, registro informativo de efectivo, inclusion/exclusion en reportes |
| Resultados | Estado de resultados por cuenta y consolidado general |
| Suscripciones | Reactivar suscripcion pausada |
| Presupuestos | Crear/listar/ajustar presupuesto mensual, soporte quincenal/semanal, reporte planeado vs real |
| Catalogos | CRUD de categorias |
| Producto | Filtros avanzados, reglas automaticas, metas, alertas/recordatorios con Kafka, adjuntos, cierre mensual, backup/restore, exportacion, seguridad |

## 6) Roadmap recomendado para "finanzas personales completas"

Orden sugerido para maximizar valor rapido con bajo riesgo:

1. V1 Control y trazabilidad bancaria:
  - PEND-TX-01, PEND-TX-02, PEND-TX-03.
  - Resultado: historial y estado de cuenta confiable por cuenta y general.
2. V2 Efectivo hibrido (como pediste):
  - PEND-CASH-01, PEND-CASH-02, PEND-CASH-03, PEND-CASH-04.
  - Resultado: retiros reales + gastos en efectivo informativos sin duplicar impacto.
3. V3 Planeacion financiera:
  - PEND-BUD-01 a PEND-BUD-06.
  - Resultado: presupuesto mensual/quincenal/semanal y seguimiento contra real.
4. V4 Resultado financiero personal:
  - PEND-RES-01, PEND-RES-02.
  - Resultado: vision ejecutiva de ingresos, gastos y neto por cuenta y global.
5. V5 Operacion madura:
  - PEND-ACC-01, PEND-ACC-02, PEND-ACC-03, PEND-TX-04, PEND-TX-05, PEND-SUB-01.
  - Resultado: ciclo completo de mantenimiento y correcciones operativas.
6. V6 Madurez de producto (sin conciliacion por ahora):
  - PEND-PROD-01 a PEND-PROD-10.
  - Resultado: app robusta para uso diario y largo plazo.

## 7) Madurez de producto (pendientes priorizados)

Nota: por decision actual, se excluye conciliacion bancaria en esta etapa.

### 7.1 Must (alto impacto)

| ID | Caso | Endpoint/Componente propuesto | Resultado esperado |
|---|---|---|---|
| PEND-PROD-01 | Filtros y busqueda avanzada de transacciones (monto, categoria, rango, contraparte, texto) | `GET /api/transactions/search` | Consultas rapidas y analisis diario eficiente |
| PEND-PROD-02 | Reglas automaticas de categorizacion | `POST/GET /api/categorization-rules` | Menos trabajo manual al registrar movimientos |
| PEND-PROD-03 | Metas de ahorro y seguimiento de avance | `POST/GET /api/savings-goals` | Planeacion de objetivos con avance medible |
| PEND-PROD-04 | Alertas y recordatorios por eventos (vencimientos, sobrepresupuesto, saldo bajo) | Kafka + `notification-service` | Alertas oportunas y accionables |
| PEND-PROD-05 | Cierre mensual y bloqueo de periodo | `POST /api/period-close` | Integridad historica y menor riesgo de cambios tardios |

### 7.2 Should (muy recomendado)

| ID | Caso | Endpoint/Componente propuesto | Resultado esperado |
|---|---|---|---|
| PEND-PROD-06 | Adjuntos por transaccion (ticket, foto, PDF) | `POST /api/transactions/{id}/attachments` | Evidencia de gastos e ingresos |
| PEND-PROD-07 | Exportacion de reportes y movimientos | `GET /api/exports/*` | Portabilidad de informacion |
| PEND-PROD-08 | Backup y restore de datos de usuario | `POST /api/backups` y `POST /api/restores` | Recuperacion ante perdida o error |

### 7.3 Nice-to-have (evolucion)

| ID | Caso | Endpoint/Componente propuesto | Resultado esperado |
|---|---|---|---|
| PEND-PROD-09 | Motor de insights personales (patrones de gasto, recomendaciones) | `GET /api/insights` | Mejor toma de decisiones financieras |
| PEND-PROD-10 | Recordatorios inteligentes adaptativos | Kafka + reglas de frecuencia/uso | Menor olvido en habitos financieros |

### 7.4 Arquitectura de eventos con Kafka (requerida)

Eventos sugeridos a publicar:

1. `transaction.created`
2. `subscription.charge.generated`
3. `budget.threshold.exceeded`
4. `account.low.balance`
5. `period.closed`

Consumidores sugeridos:

1. `notification-service` (push/email/in-app)
2. `reminder-service` (recordatorios programados)
3. `analytics-service` (metricas e insights)
4. `audit-service` (trazabilidad de eventos)

Reglas de operacion recomendadas:

1. Publicar eventos idempotentes con `eventId`.
2. Versionar esquema de evento (`eventType`, `eventVersion`).
3. Mantener reintentos y cola de errores (DLQ).
4. Evitar acoplar logica critica al envio de notificaciones (fallar notificacion no debe romper transaccion).

## 5) Checklist sugerido de smoke test

1. Crear cuenta debito (ACC-01).
2. Crear cuenta credito (ACC-02).
3. Registrar ingreso en debito (TX-02).
4. Registrar gasto en debito (TX-01).
5. Hacer traspaso a credito (TX-03).
6. Hacer transferencia a tercero (TX-04).
7. Crear suscripcion mensual (SUB-01).
8. Generar cargos del periodo (CHG-01).
9. Confirmar un cargo generado (CHG-04).
10. Consultar health (OBS-01).
