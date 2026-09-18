package service;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import dto.ExchangeRequest;
import dto.ExchangeResponse;

import java.util.List;

public interface ExchangeService {
    List<ExchangeRateResponse> getAllExchangeRates();

    ExchangeRateResponse getExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode);

    ExchangeRateResponse addExchangeRate(ExchangeRateRequest request);

    ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request);

    ExchangeResponse exchange(ExchangeRequest request);
}
