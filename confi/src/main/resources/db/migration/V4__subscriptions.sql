CREATE TABLE subscriptions (
    id              UUID PRIMARY KEY,
    nombre          VARCHAR(120)    NOT NULL,
    monto_estimado  NUMERIC(19,2)   NOT NULL,
    frecuencia      VARCHAR(20)     NOT NULL CHECK (frecuencia IN ('MENSUAL', 'BIMESTRAL', 'ANUAL')),
    dia_cobro       INT             NOT NULL CHECK (dia_cobro BETWEEN 1 AND 31),
    cuenta_id       UUID            NOT NULL REFERENCES accounts(id),
    categoria_id    UUID            NOT NULL REFERENCES categories(id),
    activa          BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE subscription_charges (
    id                      UUID PRIMARY KEY,
    subscripcion_id         UUID            NOT NULL REFERENCES subscriptions(id),
    fecha_esperada          DATE            NOT NULL,
    monto_esperado          NUMERIC(19,2)   NOT NULL,
    estado                  VARCHAR(20)     NOT NULL CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'OMITIDO')),
    transaction_id          UUID            REFERENCES transactions(id)
);

CREATE INDEX idx_subscription_charges_periodo ON subscription_charges(fecha_esperada);
CREATE INDEX idx_subscription_charges_subscripcion ON subscription_charges(subscripcion_id);