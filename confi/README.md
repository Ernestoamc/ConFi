# ConFi

API de finanzas personales construida con Spring Boot 4.1, con enfoque de arquitectura por capas (dominio + puertos + adaptadores), persistencia con JPA y migraciones con Flyway.

## Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Flyway
- PostgreSQL
- Actuator
- JUnit 6 + Mockito

## Requisitos

- Java 21
- Docker (para levantar PostgreSQL con compose)

## Configuracion

La app usa estas variables de entorno (con valores por defecto):

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/confi`)
- `DB_USERNAME` (default: `confi`)
- `DB_PASSWORD` (default: `confi`)

Archivo: `src/main/resources/application.yml`

## Levantar base de datos local

Puedes usar `compose.yaml`:

```bash
docker compose up -d
```

Nota: actualmente el compose define credenciales por defecto:

- DB: `mydatabase`
- User: `myuser`
- Password: `secret`

Si usas ese compose tal cual, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` para que coincidan.

## Ejecutar la aplicacion

En Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Puerto por defecto: `8080`

## Ejecutar tests

En Windows (PowerShell):

```powershell
.\mvnw.cmd test
```

El perfil de test usa H2 en memoria (`src/test/resources/application-test.yml`).

## Catalogo completo de endpoints

### API de negocio

| Metodo | Endpoint | Descripcion |
|---|---|---|
| POST | /api/accounts | Crea una cuenta. |
| GET | /api/accounts | Lista cuentas activas. |
| GET | /api/accounts/{id} | Obtiene una cuenta por id. |
| POST | /api/transactions | Registra una transaccion (gasto, ingreso o transferencia). |
| POST | /api/subscriptions | Crea una suscripcion. |
| GET | /api/subscriptions | Lista suscripciones activas. |
| PATCH | /api/subscriptions/{id}/pausar | Pausa una suscripcion. |
| POST | /api/subscription-charges/generar?mes={mes}&anio={anio} | Genera cargos del periodo para suscripciones activas. |
| GET | /api/subscription-charges?mes={mes}&anio={anio} | Lista cargos por mes y anio. |
| POST | /api/subscription-charges/{id}/confirmar | Confirma un cargo de suscripcion. |
| POST | /api/subscription-charges/{id}/omitir | Marca un cargo de suscripcion como omitido. |

### Observabilidad (Actuator)

Estos endpoints estan expuestos por configuracion en `management.endpoints.web.exposure.include`:

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | /actuator/health | Estado de salud de la aplicacion. |
| GET | /actuator/info | Informacion general de la aplicacion. |

## Ejemplo rapido de creacion de cuenta

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

## Estructura del proyecto

- `src/main/java/com/confi/domain`: modelo y reglas de negocio
- `src/main/java/com/confi/domain/port`: contratos de entrada/salida
- `src/main/java/com/confi/adapter/in`: controladores HTTP y health
- `src/main/java/com/confi/adapter/out`: persistencia JPA
- `src/main/resources/db/migration`: scripts Flyway

## Notas

- Flyway esta habilitado en runtime y valida el esquema antes de arrancar.
- `spring.jpa.open-in-view=false` para evitar acceso a DB fuera de frontera transaccional.
- El manejo de errores HTTP se centraliza en `GlobalExceptionHandler`.
