package mapper;

import entity.Currency;

import java.sql.ResultSet;

public class CurrencyResultSetMapper implements Mapper<Currency, ResultSet> {
    @Override
    public Currency map(ResultSet resultSet) {
        try {
            return new Currency(
                    resultSet.getInt("id"),
                    resultSet.getString("code"),
                    resultSet.getString("name"),
                    resultSet.getString("sign"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
