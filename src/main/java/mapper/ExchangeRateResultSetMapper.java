package mapper;

import entity.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateResultSetMapper implements ResultSetMapper<ExchangeRate> {
    @Override
    public ExchangeRate map(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getInt("id"),
                new CurrencyResultSetMapper().map(resultSet, "base_currency"),
                new CurrencyResultSetMapper().map(resultSet, "target_currency"),
                resultSet.getBigDecimal("rate")
        );
    }
}
