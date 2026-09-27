INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
VALUES (
    (SELECT id FROM currencies WHERE code = ?),
    (SELECT id FROM currencies WHERE code = ?),
    ?
);