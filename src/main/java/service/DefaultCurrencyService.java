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

        return currencies.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CurrencyResponse getCurrencyByCode(String code) {
        Currency requestCurrency = currenciesDao.readByCode(code);

        return toResponse(requestCurrency);
    }

    @Override
    public CurrencyResponse addCurrency(CurrencyRequest currencyRequest) {
        Currency requestCurrency = toEntity(currencyRequest);
        Currency addedCurrency = currenciesDao.create(requestCurrency);

        return toResponse(addedCurrency);
    }

    private CurrencyResponse toResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getSign()
        );
    }

    private Currency toEntity(CurrencyRequest currencyRequest) {
        return new Currency(
                0,
                currencyRequest.getCode(),
                currencyRequest.getName(),
                currencyRequest.getSign()
        );
    }
}
