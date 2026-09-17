package mapper;

import entity.Currency;

import java.sql.ResultSet;

public class MapperBaseCurrencyResultSet implements Mapper<Currency, ResultSet> {
    @Override
    public Currency map(ResultSet resultSet) {
        try {
            return new Currency(
                    resultSet.getInt("base_currency_id"),
                    resultSet.getString("base_currency_code"),
                    resultSet.getString("base_currency_name"),
                    resultSet.getString("base_currency_sign"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
