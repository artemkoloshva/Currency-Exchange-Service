package service;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;

import java.util.List;

public interface ExchangeRateService {
    List<ExchangeRateResponse> getAllExchangeRates();

    ExchangeRateResponse getExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode);

    ExchangeRateResponse addExchangeRate(ExchangeRateRequest request);

    ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request);
}
