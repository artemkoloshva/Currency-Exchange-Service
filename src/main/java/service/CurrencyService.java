package service;

import dto.CurrencyRequest;
import dto.CurrencyResponse;

import java.util.List;

public interface CurrencyService {
    List<CurrencyResponse> getAllCurrencies();

    CurrencyResponse getCurrencyByCode(String code);

    CurrencyResponse addCurrency(CurrencyRequest request);
}
