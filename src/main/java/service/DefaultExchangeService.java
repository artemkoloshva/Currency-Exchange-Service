package service;

import dao.ExchangeRatesDao;
import dto.*;
import entity.Currency;
import entity.ExchangeRate;
import exception.BadRequestException;
import exception.NotFoundException;

import java.util.List;
import java.util.Optional;

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
        String baseCurrencyCode = request.getBaseCurrencyCode().toUpperCase();
        String targetCurrencyCode = request.getTargetCurrencyCode().toUpperCase();

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new BadRequestException(
                    "Base and target currencies must be different, but both are: " + baseCurrencyCode);
        }

        if (request.getRate() <= 0) {
            throw new BadRequestException(
                    "Exchange rate must be greater than 0, but got: " + request.getRate());
        }

        if (hasMirrorExchangeRate(baseCurrencyCode, targetCurrencyCode)) {
            throw new BadRequestException(
                    "Mirror exchange rate already exists: " + targetCurrencyCode + "/" + baseCurrencyCode);
        }

        ExchangeRate addedExchangeRate = exchangeRatesDao.createByCodes(
                baseCurrencyCode,
                targetCurrencyCode,
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
        String baseCode = request.getBaseCurrencyCode();
        String targetCode = request.getTargetCurrencyCode();
        float amount = request.getAmount();

        ExchangeRate rate = findRate(baseCode, targetCode)
                .or(() -> findRateViaUsd(baseCode, targetCode))
                .orElseThrow(() -> new NotFoundException(
                        "Exchange rate not found for " + baseCode + " to " + targetCode));

        return new ExchangeResponse(
                toResponse(rate.getBaseCurrency()),
                toResponse(rate.getTargetCurrency()),
                rate.getRate(),
                amount,
                rate.getRate() * amount
        );
    }

    private boolean hasMirrorExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {
        try {
            exchangeRatesDao.readByCurrencyCodes(targetCurrencyCode, baseCurrencyCode);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private Optional<ExchangeRate> findRate(String from, String to) {
        ExchangeRate direct = tryGetRate(from, to);
        if (direct != null) {
            return Optional.of(direct);
        }

        ExchangeRate reverse = tryGetRate(to, from);
        if (reverse != null) {
            return Optional.of(invert(reverse));
        }

        return Optional.empty();
    }

    private Optional<ExchangeRate> findRateViaUsd(String from, String to) {
        Optional<ExchangeRate> usdToFrom = findRate("USD", from);
        Optional<ExchangeRate> usdToTo = findRate("USD", to);

        if (usdToFrom.isEmpty() || usdToTo.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate a = usdToFrom.get();
        ExchangeRate b = usdToTo.get();

        return Optional.of(new ExchangeRate(
                0,
                a.getTargetCurrency(),
                b.getTargetCurrency(),
                b.getRate() / a.getRate()
        ));
    }

    private ExchangeRate invert(ExchangeRate rate) {
        return new ExchangeRate(
                rate.getId(),
                rate.getTargetCurrency(),
                rate.getBaseCurrency(),
                1.0f / rate.getRate()
        );
    }

    private ExchangeRate tryGetRate(String baseCode, String targetCode) {
        try {
            return exchangeRatesDao.readByCurrencyCodes(baseCode, targetCode);
        } catch (NotFoundException e) {
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
