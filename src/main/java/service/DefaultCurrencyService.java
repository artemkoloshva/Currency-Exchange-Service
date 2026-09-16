package service;

import dto.CurrencyRequest;
import dto.CurrencyResponse;

import java.util.List;

public class DefaultCurrencyService implements CurrencyService{

    @Override
    public List<CurrencyResponse> getAllCurrencies() {
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
