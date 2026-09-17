SELECT
    er.id,
    er.rate,
    bc.id AS base_currency_id, bc.code AS base_currency_code, bc.name AS base_currency_name, bc.sign AS base_currency_sign,
    tc.id AS target_currency_id, tc.code AS target_currency_code, tc.name AS target_currency_name, tc.sign AS target_currency_sign
FROM exchange_rates er
    JOIN currencies bc ON er.base_currency_code = bc.code
    JOIN currencies tc ON er.target_currency_code = tc.code;