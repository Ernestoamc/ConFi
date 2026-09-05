# ConFi

ConFi es una API de finanzas personales con trazabilidad contable.

## Capacidades actuales

- Cuentas debito y credito.
- Movimientos (gasto, ingreso, transferencia) con saldo resultante por transaccion.
- Estado de cuenta por cuenta y consolidado.
- Suscripciones y cargos periodicos.
- Presupuestos mensual, semanal y quincenal.
- Estado de resultados y comparativo presupuesto vs real.
- Eventos de dominio por Kafka (opcional).
- Bandeja local de notificaciones (in-memory), con gestion de leidas/no leidas.
- Alerta automatica de saldo bajo en cuentas debito.
- Alerta automatica cuando se excede el presupuesto de categoria.
- Recordatorio automatico de suscripciones proximas a vencer.
- Metas de ahorro con avance acumulado.
- Cierre mensual de periodo para bloquear cambios historicos en transacciones, presupuestos y cargos de suscripcion.
- Adjuntos de transacciones (metadata de archivo/URL).
- Backup/restore integral base del sistema.

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring WebMVC
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Flyway
- PostgreSQL (runtime)
- H2 (tests)
- Kafka (eventos opcionales)

## Ejecucion local (Windows)

1. Levantar PostgreSQL (por ejemplo con Docker Compose).
2. Ajustar variables de entorno si necesitas cambiar defaults.
3. Ejecutar la app:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Ejecutar pruebas:

```powershell
.\mvnw.cmd test
```

## Configuracion

Variables de base de datos:

- DB_URL (default: jdbc:postgresql://localhost:5432/confi)
- DB_USERNAME (default: confi)
- DB_PASSWORD (default: confi)

Variables de eventos Kafka:

- KAFKA_EVENTS_ENABLED (default: false)
- KAFKA_EVENTS_TOPIC (default: confi.events)
- KAFKA_EVENTS_GROUP_ID (default: confi.notification.local)
- KAFKA_BOOTSTRAP_SERVERS (default: localhost:9092)

Variables de recordatorios:

- REMINDER_EVENTS_ENABLED (default: true)
- REMINDER_DAYS_AHEAD (default: 3)
- REMINDER_CRON (default: 0 0 8 * * *)

Comportamiento importante:

- Si KAFKA_EVENTS_ENABLED=false, el publicador usa estrategia no-op.
- Si Kafka falla al publicar, no se interrumpe la transaccion principal.

## Endpoints

### Cuentas

- POST /api/accounts
- GET /api/accounts
- GET /api/accounts/{id}
- PATCH /api/accounts/{id}
- PATCH /api/accounts/{id}/desactivar
- PATCH /api/accounts/{id}/reactivar

### Transacciones

- POST /api/transactions
- GET /api/transactions?desde={isoInstant}&hasta={isoInstant}&cuentaId={uuid?}
- GET /api/transactions/search?desde={isoInstant}&hasta={isoInstant}&cuentaId={uuid?}&tipo={GASTO|INGRESO|TRANSFERENCIA?}&categoriaId={uuid?}&contraparte={txt?}&texto={txt?}&montoMin={n?}&montoMax={n?}
- PATCH /api/transactions/{id}
- POST /api/transactions/{id}/cancel
- PUT /api/transactions/{id}

### Reglas de categorizacion

- POST /api/categorization-rules
- GET /api/categorization-rules
- PATCH /api/categorization-rules/{id}/activate
- PATCH /api/categorization-rules/{id}/deactivate
- GET /api/categorization-rules/resolve?text={txt}

### Metas de ahorro

- POST /api/savings-goals
- GET /api/savings-goals
- PATCH /api/savings-goals/{id}/progress
- PATCH /api/savings-goals/{id}/deactivate

### Cierre de periodo

- POST /api/period-close
- PATCH /api/period-close/reopen
- GET /api/period-close

### Adjuntos por transaccion

- POST /api/transactions/{transactionId}/attachments
- GET /api/transactions/{transactionId}/attachments

### Estado de cuenta

- GET /api/accounts/{id}/statement?desde={isoInstant}&hasta={isoInstant}
- GET /api/statement?desde={isoInstant}&hasta={isoInstant}

### Efectivo

- POST /api/cash/withdrawals
- POST /api/cash/transactions
- GET /api/cash/transactions?desde={isoInstant}&hasta={isoInstant}

### Suscripciones y cargos

- POST /api/subscriptions
- GET /api/subscriptions
- PATCH /api/subscriptions/{id}/pausar
- PATCH /api/subscriptions/{id}/reactivar
- POST /api/subscription-charges/generar?mes={1..12}&anio={yyyy}
- GET /api/subscription-charges?mes={1..12}&anio={yyyy}
- POST /api/subscription-charges/{id}/confirmar
- POST /api/subscription-charges/{id}/omitir

### Presupuestos y reportes

- POST /api/budgets
- GET /api/budgets?mes={1..12}&anio={yyyy}
- PATCH /api/budgets/{id}
- POST /api/budgets/weekly
- GET /api/budgets/weekly?desde={yyyy-MM-dd}&hasta={yyyy-MM-dd}
- POST /api/budgets/biweekly
- GET /api/budgets/biweekly?desde={yyyy-MM-dd}&hasta={yyyy-MM-dd}
- GET /api/reports/income-statement?desde={isoInstant}&hasta={isoInstant}&cuentaId={uuid?}
- GET /api/reports/budget-vs-actual?desde={yyyy-MM-dd}&hasta={yyyy-MM-dd}&scope={MENSUAL|SEMANAL|QUINCENAL|TODOS}

### Notificaciones

- GET /api/notifications?limit={1..200}
- PATCH /api/notifications/{id}/read
- POST /api/notifications/read-all
- GET /api/notifications/summary
- DELETE /api/notifications

### Export y backup/restore

- GET /api/exports/transactions.csv?desde={isoInstant}&hasta={isoInstant}&cuentaId={uuid?}
- GET /api/backups/notifications
- POST /api/restores/notifications
- GET /api/backups/system
- POST /api/restores/system

## Eventos de dominio publicados

- transaction.created
- subscription.charge.generated
- subscription.charge.confirmed
- account.low.balance
- budget.threshold.exceeded
- subscription.charge.due.soon

## Observabilidad

- GET /actuator/health
- GET /actuator/info

## Documentacion funcional

- docs/matriz-casos-uso.md
