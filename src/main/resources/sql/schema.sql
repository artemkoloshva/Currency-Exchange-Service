CREATE TABLE IF NOT EXISTS currencies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE CHECK (LENGTH(code) = 3),
    name TEXT NOT NULL,
    sign TEXT CHECK (LENGTH(sign) <= 5)
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_code TEXT NOT NULL CHECK (LENGTH(base_currency_code) = 3),
    target_currency_code TEXT NOT NULL CHECK (LENGTH(target_currency_code) = 3),
    rate NUMERIC NOT NULL CHECK (rate > 0),
    FOREIGN KEY (base_currency_code) REFERENCES currencies(code),
    FOREIGN KEY (target_currency_code) REFERENCES currencies(code),
    UNIQUE (base_currency_code, target_currency_code)
);