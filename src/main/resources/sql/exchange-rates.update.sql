UPDATE exchange_rates
SET rate = ?
WHERE base_currency_code = ? AND target_currency_code = ?;