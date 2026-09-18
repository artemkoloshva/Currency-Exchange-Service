INSERT INTO currencies (code, name, sign) VALUES
('USD', 'United States Dollar', '$'),
('RUB', 'Russian Ruble', '₽'),
('EUR', 'Euro', '€'),
('GBP', 'British Pound Sterling', '£'),
('JPY', 'Japanese Yen', '¥'),
('AUD', 'Australian Dollar', '$'),
('CAD', 'Canadian Dollar', '$'),
('CHF', 'Swiss Franc', 'CHF'),
('CNY', 'Chinese Yuan Renminbi', '¥'),
('SEK', 'Swedish Krona', 'kr'),
('NZD', 'New Zealand Dollar', '$');

INSERT INTO exchange_rates (base_currency_code, target_currency_code, rate) VALUES
('USD', 'EUR', 0.85),
('USD', 'GBP', 0.75),
('USD', 'JPY', 110.0),
('USD', 'AUD', 1.35),
('USD', 'CAD', 1.25),
('USD', 'CHF', 0.92),
('USD', 'CNY', 6.45),
('USD', 'SEK', 8.65),
('USD', 'NZD', 1.42),
('USD', 'RUB', 75.0);