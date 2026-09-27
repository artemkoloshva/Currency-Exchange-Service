package mapper;

import entity.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class CurrencyResultSetMapper implements ResultSetMapper<Currency> {
    @Override
    public Currency map(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getString("sign")
        );
    }

    public Currency map(ResultSet resultSet, String prefix) throws SQLException {
        return new Currency(
                resultSet.getInt(prefix + "_id"),
                resultSet.getString(prefix + "_code"),
                resultSet.getString(prefix + "_name"),
                resultSet.getString(prefix + "_sign")
        );
    }
}
