package dao;

import entity.Currency;
import entity.ExchangeRate;
import exception.ConflictException;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import util.SqlLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JdbcExchangeRatesDao implements ExchangeRatesDao {
    private static final String SQL_CREATE = SqlLoader.loadSqlQuery("exchange-rates.create.sql");
    private static final String SQL_READ_BY_CURRENCY_CODES = SqlLoader.loadSqlQuery("exchange-rates.read-by-currency-codes.sql");
    private static final String SQL_READ = SqlLoader.loadSqlQuery("exchange-rates.read.sql");
    private static final String SQL_READ_ALL = SqlLoader.loadSqlQuery("exchange-rates.read-all.sql");
    private static final String SQL_UPDATE = SqlLoader.loadSqlQuery("exchange-rates.update.sql");
    private static final String SQL_DELETE = SqlLoader.loadSqlQuery("exchange-rates.delete.sql");

    private final Connection connection;

    public JdbcExchangeRatesDao() {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("db.properties"));
            String dbUrl = properties.getProperty("url");

            connection = DriverManager.getConnection(dbUrl);
        } catch (NullPointerException e) {
            throw new NullPointerException("db.properties not found");
        } catch (SQLException e) {
            throw new InternalServerErrorException("Failed to connect to the database");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExchangeRate create(ExchangeRate entity) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_CREATE)) {
            statement.setString(1, entity.getBaseCurrency().getCode());
            statement.setString(2, entity.getTargetCurrency().getCode());
            statement.setFloat(3, entity.getRate());

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "The inserted exchange rate already exists: " + entity.getBaseCurrency().getCode() + "/" + entity.getTargetCurrency().getCode());
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error creating exchange rate: " + entity.getBaseCurrency().getCode() + "/" + entity.getTargetCurrency().getCode());
        }

        return entity;
    }

    @Override
    public ExchangeRate read(int id) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Exchange rate with id " + id + " not found");
            }

            return mapExchangeRate(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading exchange rate with ID: " + id);
        }
    }

    @Override
    public List<ExchangeRate> readAll() {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();

            while (resultSet.next()) {
                exchangeRates.add(mapExchangeRate(resultSet));
            }

            return exchangeRates;
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading all exchange rates");
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate entity) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setFloat(1, entity.getRate());
            statement.setString(2, entity.getBaseCurrency().getCode());
            statement.setString(3, entity.getTargetCurrency().getCode());
            statement.executeUpdate();

            return read(entity.getId());
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error updating exchange rate with code: " + entity.getBaseCurrency().getCode() + "/" + entity.getTargetCurrency().getCode());
        }
    }

    @Override
    public void delete(int id) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Exchange rate with ID " + id + " not found");
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error deleting exchange rate with ID: " + id);
        }
    }

    @Override
    public ExchangeRate readByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ_BY_CURRENCY_CODES)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode + " not found");
            }

            return mapExchangeRate(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    public ExchangeRate createByCodes(String baseCurrencyCode, String targetCurrencyCode, float rate) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_CREATE)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            statement.setFloat(3, rate);

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "Error creating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
            }

            return readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error creating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    public ExchangeRate updateByCodes(String baseCurrencyCode, String targetCurrencyCode, float rate) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setFloat(1, rate);
            statement.setString(2, baseCurrencyCode);
            statement.setString(3, targetCurrencyCode);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode + " not found");
            }

            return readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error updating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    private Currency mapBaseCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("base_currency_id"),
                resultSet.getString("base_currency_code"),
                resultSet.getString("base_currency_name"),
                resultSet.getString("base_currency_sign")
        );
    }

    private Currency mapTargetCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("target_currency_id"),
                resultSet.getString("target_currency_code"),
                resultSet.getString("target_currency_name"),
                resultSet.getString("target_currency_sign")
        );
    }

    private ExchangeRate mapExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getInt("id"),
                mapBaseCurrency(resultSet),
                mapTargetCurrency(resultSet),
                resultSet.getFloat("rate")
        );
    }
}