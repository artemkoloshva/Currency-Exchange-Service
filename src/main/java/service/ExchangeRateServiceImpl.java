package service;

import dao.ExchangeRateDao;
import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import entity.ExchangeRate;
import exception.BadRequestException;
import mapper.ExchangeRateMapper;

import java.util.List;

public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateServiceImpl(ExchangeRateDao exchangeRateDao, ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateDao = exchangeRateDao;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Override
    public List<ExchangeRateResponse> getAllExchangeRates() {
        return exchangeRateMapper.toResponseList(exchangeRateDao.readAll());
    }

    @Override
    public ExchangeRateResponse getExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateMapper.toResponse(exchangeRateDao.readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode));
    }

    @Override
    public ExchangeRateResponse addExchangeRate(ExchangeRateRequest request) {
        return exchangeRateMapper.toResponse(exchangeRateDao.createByCodes(
                request.baseCurrencyCode(),
                request.targetCurrencyCode(),
                request.rate()
        ));
    }

    @Override
    public ExchangeRateResponse updateExchangeRate(ExchangeRateRequest request) {
        return exchangeRateMapper.toResponse(exchangeRateDao.updateByCodes(
                request.baseCurrencyCode(),
                request.targetCurrencyCode(),
                request.rate())
        );
    }
}
