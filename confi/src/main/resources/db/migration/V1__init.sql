CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    nombre          VARCHAR(120)    NOT NULL,
    tipo            VARCHAR(20)     NOT NULL CHECK (tipo IN ('DEBITO', 'CREDITO')),
    saldo           NUMERIC(19,4)   NOT NULL,
    limite_credito  NUMERIC(19,4),
    activa          BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE categories (
    id                  UUID PRIMARY KEY,
    nombre              VARCHAR(120)    NOT NULL,
    tipo                VARCHAR(20)     NOT NULL CHECK (tipo IN ('GASTO', 'INGRESO')),
    icono               VARCHAR(60),
    es_subscripcion     BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE TABLE transactions (
    id                  UUID PRIMARY KEY,
    fecha               TIMESTAMPTZ     NOT NULL,
    monto               NUMERIC(19,4)   NOT NULL,
    nota                VARCHAR(500),
    tipo                VARCHAR(20)     NOT NULL CHECK (tipo IN ('GASTO', 'INGRESO', 'TRANSFERENCIA')),
    cuenta_origen_id    UUID            NOT NULL REFERENCES accounts(id),
    cuenta_destino_id   UUID            REFERENCES accounts(id),
    categoria_id        UUID            REFERENCES categories(id),
    contraparte         VARCHAR(200),
    subscripcion_id     UUID
);

CREATE INDEX idx_transactions_fecha ON transactions(fecha);
CREATE INDEX idx_transactions_categoria ON transactions(categoria_id);

CREATE TABLE budgets (
    id              UUID PRIMARY KEY,
    mes             INT             NOT NULL CHECK (mes BETWEEN 1 AND 12),
    anio            INT             NOT NULL,
    categoria_id    UUID            NOT NULL REFERENCES categories(id),
    monto_planeado  NUMERIC(19,4)   NOT NULL,
    UNIQUE (categoria_id, mes, anio)
);