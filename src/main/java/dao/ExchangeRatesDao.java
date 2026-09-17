package dao;

import dto.ExchangeRateRequest;
import entity.ExchangeRate;

public interface ExchangeRatesDao extends Dao<ExchangeRate>{
    ExchangeRate readByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);
    ExchangeRate createByCodes(String baseCurrencyCode, String targetCurrencyCode, float rate);
    ExchangeRate updateByCodes(String baseCurrencyCode, String targetCurrencyCode, float rate);
}
