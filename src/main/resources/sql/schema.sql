CREATE TABLE IF NOT EXISTS currencies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE CHECK (LENGTH(code) = 3),
    name TEXT NOT NULL,
    sign TEXT CHECK (LENGTH(sign) <= 5)
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id INTEGER NOT NULL,
    target_currency_id INTEGER NOT NULL,
    rate NUMERIC NOT NULL CHECK (rate > 0),
    FOREIGN KEY (base_currency_id) REFERENCES currencies(id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(id),
    UNIQUE (base_currency_id, target_currency_id)
);