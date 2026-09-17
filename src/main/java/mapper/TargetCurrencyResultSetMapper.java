package mapper;

import entity.Currency;

import java.sql.ResultSet;

public class TargetCurrencyResultSetMapper implements Mapper<Currency, ResultSet> {
    @Override
    public Currency map(ResultSet resultSet) {
        try {
            return new Currency(
                    resultSet.getInt("target_currency_id"),
                    resultSet.getString("target_currency_code"),
                    resultSet.getString("target_currency_name"),
                    resultSet.getString("target_currency_sign"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
