package dao;

import entity.ExchangeRate;

public interface ExchangeRatesDao extends Dao<ExchangeRate>{
    ExchangeRate readByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);
}
