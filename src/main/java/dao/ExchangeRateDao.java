package dao;

import entity.ExchangeRate;

import java.math.BigDecimal;

public interface ExchangeRateDao extends Dao<ExchangeRate>{
    ExchangeRate readByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);
    ExchangeRate createByCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);
    ExchangeRate updateByCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate);
}
