package mapper;

import entity.ExchangeRate;

import java.sql.ResultSet;

public class ExchangeRateResultSetMapper implements Mapper<ExchangeRate, ResultSet> {
    @Override
    public ExchangeRate map(ResultSet resultSet) {
        try {
            return new ExchangeRate(
                    resultSet.getInt("id"),
                    new BaseCurrencyResultSetMapper().map(resultSet),
                    new TargetCurrencyResultSetMapper().map(resultSet),
                    resultSet.getFloat("rate")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
