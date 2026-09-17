package service;

import dao.CurrenciesDao;
import dao.ExchangeRatesDao;
import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import entity.ExchangeRate;

import java.util.List;

public class DefaultExchangeRateService implements ExchangeRateService {
    private final ExchangeRatesDao exchangeRatesDao;

    public DefaultExchangeRateService (ExchangeRatesDao exchangeRatesDao) {
        this.exchangeRatesDao = exchangeRatesDao;
    }

    @Override
    public List<ExchangeRateResponse> getAllExchangeRates() {
        List<ExchangeRate> exchangeRates = exchangeRatesDao.readAll();

        return exchangeRates.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ExchangeRateResponse getExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        ExchangeRate exchangeRate = exchangeRatesDao.readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);

        return toResponse(exchangeRate);
    }

    @Override
    public ExchangeRateResponse addExchangeRate(ExchangeRateRequest request) {
        ExchangeRate addedExchangeRate = exchangeRatesDao.createByCodes(
                request.getBaseCurrencyCode().toUpperCase(),
                request.getTargetCurrencyCode().toUpperCase(),
                request.getRate()
        );

        return toResponse(addedExchangeRate);
    }

    @Override
    public ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request) {
        ExchangeRate updatedExchangeRate = exchangeRatesDao.updateByCodes(
                request.getBaseCurrencyCode(),
                request.getTargetCurrencyCode(),
                request.getRate()
        );

        return toResponse(updatedExchangeRate);
    }

    private ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getId(),
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getTargetCurrency().getCode(),
                exchangeRate.getRate()
        );
    }
}
