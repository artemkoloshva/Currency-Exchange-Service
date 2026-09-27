package dao;

import entity.Currency;

public interface CurrencyDao extends Dao<Currency> {
    Currency readByCode(String code);
    Currency updateByCode(Currency currency);
}
