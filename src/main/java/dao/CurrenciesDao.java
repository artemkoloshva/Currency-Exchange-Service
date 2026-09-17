package dao;

import entity.Currency;

public interface CurrenciesDao extends Dao<Currency> {
    Currency readByCode(String code);
}
