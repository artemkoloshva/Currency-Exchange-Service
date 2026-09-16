package service;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;

import java.util.List;

public class DefaultExchangeRateService implements ExchangeRateService {
    @Override
    public List<ExchangeRateResponse> getAllExchangeRates() {
        return null;
    }

    @Override
    public ExchangeRateResponse getExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        return null;
    }

    @Override
    public ExchangeRateResponse addExchangeRate(ExchangeRateRequest request) {
        return null;
    }

    @Override
    public ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request) {
        return null;
    }
}
