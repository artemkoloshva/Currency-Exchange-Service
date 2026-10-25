package service;

import dao.ExchangeRateDao;
import dto.*;
import entity.ExchangeRate;
import exception.NotFoundException;
import mapper.CurrencyMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeServiceImpl implements ExchangeService {
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyMapper currencyMapper;

    public ExchangeServiceImpl(ExchangeRateDao exchangeRateDao, CurrencyMapper currencyMapper) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyMapper = currencyMapper;
    }

    public ExchangeResponse exchange(ExchangeRequest request) {
        String baseCode = request.baseCurrencyCode();
        String targetCode = request.targetCurrencyCode();
        BigDecimal amount = request.amount();

        ExchangeRate exchangeRate = findRate(baseCode, targetCode)
                .or(() -> findRateViaUsd(baseCode, targetCode))
                .orElseThrow(() -> new NotFoundException(
                        "Exchange rate not found for " + baseCode + " to " + targetCode));

        return new ExchangeResponse(
                currencyMapper.toResponse(exchangeRate.getBaseCurrency()),
                currencyMapper.toResponse(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate(),
                amount,
                exchangeRate.getRate().multiply(amount).setScale(2, RoundingMode.HALF_UP)
        );
    }

    private boolean hasMirrorExchangeRate(String baseCurrencyCode, String targetCurrencyCode) {
        try {
            exchangeRateDao.readByCurrencyCodes(targetCurrencyCode, baseCurrencyCode);
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
                b.getRate().divide(a.getRate(), 6, RoundingMode.HALF_UP)
        ));
    }

    private ExchangeRate invert(ExchangeRate rate) {
        return new ExchangeRate(
                rate.getId(),
                rate.getTargetCurrency(),
                rate.getBaseCurrency(),
                BigDecimal.ONE.divide(rate.getRate(), 6, RoundingMode.HALF_UP)
        );
    }

    private ExchangeRate tryGetRate(String baseCode, String targetCode) {
        try {
            return exchangeRateDao.readByCurrencyCodes(baseCode, targetCode);
        } catch (NotFoundException e) {
            return null;
        }
    }
}
