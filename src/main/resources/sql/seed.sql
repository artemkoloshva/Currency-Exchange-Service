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

INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES
(1, 3, 0.85),
(1, 4, 0.75),
(1, 5, 110.0),
(1, 6, 1.35),
(1, 7, 1.25),
(1, 8, 0.92),
(1, 9, 6.45),
(1, 10, 8.65),
(1, 11, 1.42),
(1, 2, 0.014);