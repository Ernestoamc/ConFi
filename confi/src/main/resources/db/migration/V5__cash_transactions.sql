CREATE TABLE cash_transactions (
    id              UUID PRIMARY KEY,
    fecha           TIMESTAMPTZ     NOT NULL,
    movimiento      VARCHAR(20)     NOT NULL CHECK (movimiento IN ('CARGO', 'ABONO')),
    monto           NUMERIC(19,2)   NOT NULL,
    nota            VARCHAR(500),
    categoria_id    UUID            REFERENCES categories(id),
    contraparte     VARCHAR(200),
    impacta_saldo   BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_cash_transactions_fecha ON cash_transactions(fecha);
CREATE INDEX idx_cash_transactions_categoria ON cash_transactions(categoria_id);
