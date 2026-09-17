package service;

import dao.CurrenciesDao;
import dto.CurrencyRequest;
import dto.CurrencyResponse;
import entity.Currency;

import java.util.List;

public class DefaultCurrencyService implements CurrencyService{
    private final CurrenciesDao currenciesDao;

    public DefaultCurrencyService(CurrenciesDao currenciesDao) {
        this.currenciesDao = currenciesDao;
    }

    @Override
    public List<CurrencyResponse> getAllCurrencies() {
        List<Currency> currencies = currenciesDao.readAll();
        return null;
    }

    @Override
    public CurrencyResponse getCurrencyByCode(String code) {
        return null;
    }

    @Override
    public CurrencyResponse addCurrency(CurrencyRequest currencyRequest) {
        return null;
    }
}
