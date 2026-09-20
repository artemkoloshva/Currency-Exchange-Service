package service;

import dao.ExchangeRatesDao;
import dto.*;
import entity.Currency;
import entity.ExchangeRate;
import exception.BadRequestException;
import exception.NotFoundException;

import java.util.List;

public class DefaultExchangeService implements ExchangeService {
    private final ExchangeRatesDao exchangeRatesDao;

    public DefaultExchangeService(ExchangeRatesDao exchangeRatesDao) {
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
        if (request.getBaseCurrencyCode().equals(request.getTargetCurrencyCode())) {
            throw new BadRequestException(
                    "Base and target currencies must be different, but both are: " + request.getBaseCurrencyCode());
        }

        if (request.getRate() <= 0) {
            throw new BadRequestException(
                    "Exchange rate must be greater than 0, but got: " + request.getRate());
        }

        ExchangeRate addedExchangeRate = exchangeRatesDao.createByCodes(
                request.getBaseCurrencyCode().toUpperCase(),
                request.getTargetCurrencyCode().toUpperCase(),
                request.getRate()
        );

        return toResponse(addedExchangeRate);
    }

    @Override
    public ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request) {
        if (request.getRate() <= 0) {
            throw new BadRequestException(
                    "ExchangeRate validation failed: rate must be greater than 0, but got " + request.getRate());
        }

        ExchangeRate updatedExchangeRate = exchangeRatesDao.updateByCodes(
                request.getBaseCurrencyCode(),
                request.getTargetCurrencyCode(),
                request.getRate()
        );

        return toResponse(updatedExchangeRate);
    }

    public ExchangeResponse exchange(ExchangeRequest request) {
        String baseCurrencyCode = request.getBaseCurrencyCode();
        String targetCurrencyCode = request.getTargetCurrencyCode();
        float amount = request.getAmount();

        ExchangeRate direct = tryGetRate(baseCurrencyCode, targetCurrencyCode);

        if (direct != null) {
            float convertedAmount = convertCurrency(direct, amount);
            return toResponse(direct, amount, convertedAmount);
        }

        direct = tryGetRate(targetCurrencyCode, baseCurrencyCode);

        if (direct != null) {
            float convertedAmount = convertCurrency(direct, amount);
            return toResponse(direct, amount, convertedAmount);
        }

        ExchangeRate baseToUsd = tryGetRate(baseCurrencyCode, "USD");
        ExchangeRate usdToTarget = tryGetRate("USD", targetCurrencyCode);

        if (baseToUsd != null && usdToTarget != null) {
            float convertedAmount = convertCurrency(baseToUsd, amount);
            convertedAmount = convertCurrency(usdToTarget, convertedAmount);
            return toResponse(baseToUsd, amount, convertedAmount);
        }

        throw new NotFoundException("Exchange rate not found for " + baseCurrencyCode + " to " + targetCurrencyCode);
    }

    private float convertCurrency(ExchangeRate exchangeRate, float amount) {
        return exchangeRate.getRate() * amount;
    }

    private ExchangeRate tryGetRate(String baseCode, String targetCode) {
        try {
            return exchangeRatesDao.readByCurrencyCodes(baseCode, targetCode);
        }
        catch (NotFoundException e) {
            return null;
        }
    }

    private ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getId(),
                toResponse(exchangeRate.getBaseCurrency()),
                toResponse(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate()
        );
    }
    
    private CurrencyResponse toResponse(Currency currency) {
        return new CurrencyResponse(
                currency.getId(),
                currency.getCode(),
                currency.getName(),
                currency.getSign()
        );
    }
    
    private ExchangeResponse toResponse(ExchangeRate exchangeRate, float amount, float convertedAmount) {
        return new ExchangeResponse(
                toResponse(exchangeRate.getBaseCurrency()),
                toResponse(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }
}
