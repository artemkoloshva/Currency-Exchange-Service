package dao;

import entity.Currency;
import entity.ExchangeRate;
import exception.ConflictException;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import mapper.MapperExchangeRateResultSet;
import util.SqlLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JdbcExchangeRatesDao implements ExchangeRatesDao {
    private static final String SQL_CREATE = SqlLoader.loadSqlQuery("exchange-rate.create.sql");
    private static final String SQL_READ_BY_CURRENCY_CODES = SqlLoader.loadSqlQuery("exchange-rate.read-by-currency-codes.sql");
    private static final String SQL_READ = SqlLoader.loadSqlQuery("exchange-rate.read.sql");
    private static final String SQL_READ_ALL = SqlLoader.loadSqlQuery("exchange-rate.read-all.sql");
    private static final String SQL_UPDATE = SqlLoader.loadSqlQuery("exchange-rate.update.sql");
    private static final String SQL_DELETE = SqlLoader.loadSqlQuery("exchange-rate.delete.sql");

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

            return new MapperExchangeRateResultSet().map(resultSet);
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
                exchangeRates.add(new MapperExchangeRateResultSet().map(resultSet));
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

            return new MapperExchangeRateResultSet().map(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);

        }
    }
}