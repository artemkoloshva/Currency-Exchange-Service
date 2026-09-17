package mapper;

import entity.Currency;
import entity.ExchangeRate;

import java.sql.ResultSet;

public class MapperExchangeRateResultSet implements Mapper<ExchangeRate, ResultSet> {
    @Override
    public ExchangeRate map(ResultSet resultSet) {
        try {
            return new ExchangeRate(
                    resultSet.getInt("id"),
                    new MapperBaseCurrencyResultSet().map(resultSet),
                    new MapperTargetCurrencyResultSet().map(resultSet),
                    resultSet.getFloat("rate")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
