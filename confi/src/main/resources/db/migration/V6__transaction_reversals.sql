CREATE TABLE transaction_reversals (
    id                          UUID PRIMARY KEY,
    original_transaction_id     UUID NOT NULL UNIQUE REFERENCES transactions(id),
    reversal_transaction_id     UUID NOT NULL UNIQUE REFERENCES transactions(id),
    created_at                  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transaction_reversals_original ON transaction_reversals(original_transaction_id);
CREATE INDEX idx_transaction_reversals_reversal ON transaction_reversals(reversal_transaction_id);
