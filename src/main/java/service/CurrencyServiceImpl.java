package service;

import dao.CurrencyDao;
import dto.CurrencyRequest;
import dto.CurrencyResponse;
import entity.Currency;
import mapper.CurrencyMapper;

import java.util.List;

public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyDao currenciesDao;
    private final CurrencyMapper currencyMapper;

    public CurrencyServiceImpl(CurrencyDao currenciesDao, CurrencyMapper currencyMapper) {
        this.currenciesDao = currenciesDao;
        this.currencyMapper = currencyMapper;
    }

    @Override
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyMapper.toResponseList(currenciesDao.readAll());
    }

    @Override
    public CurrencyResponse getCurrencyByCode(String code) {
        return currencyMapper.toResponse(currenciesDao.readByCode(code));
    }

    @Override
    public CurrencyResponse addCurrency(CurrencyRequest request) {
        return currencyMapper.toResponse(currenciesDao.create(currencyMapper.toEntity(request)));
    }
}
