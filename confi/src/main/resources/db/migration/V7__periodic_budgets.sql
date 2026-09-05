CREATE TABLE periodic_budgets (
    id              UUID PRIMARY KEY,
    period_type     VARCHAR(20)     NOT NULL CHECK (period_type IN ('SEMANAL', 'QUINCENAL')),
    desde           DATE            NOT NULL,
    hasta           DATE            NOT NULL,
    categoria_id    UUID            NOT NULL REFERENCES categories(id),
    monto_planeado  NUMERIC(19,2)   NOT NULL,
    CHECK (desde <= hasta)
);

CREATE INDEX idx_periodic_budgets_type_range ON periodic_budgets(period_type, desde, hasta);
CREATE INDEX idx_periodic_budgets_categoria ON periodic_budgets(categoria_id);
