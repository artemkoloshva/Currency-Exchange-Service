UPDATE exchange_rates
SET rate = ?
WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
  AND target_currency_id = (SELECT id FROM currencies WHERE code = ?);