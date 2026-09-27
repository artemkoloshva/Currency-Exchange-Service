package dao;

import entity.ExchangeRate;
import exception.ConflictException;
import exception.DatabaseException;
import exception.NotFoundException;
import mapper.ExchangeRateResultSetMapper;
import mapper.ResultSetMapper;
import util.DataSourceProvider;
import util.SqlLoader;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExchangeRateDao implements ExchangeRateDao {
    private static final String SQL_CREATE = SqlLoader.loadSqlQuery("exchange-rates.create.sql");
    private static final String SQL_CREATE_BY_CURRENCY_CODES = SqlLoader.loadSqlQuery("exchange-rates.create-by-currency-codes.sql");
    private static final String SQL_READ = SqlLoader.loadSqlQuery("exchange-rates.read.sql");
    private static final String SQL_READ_ALL = SqlLoader.loadSqlQuery("exchange-rates.read-all.sql");
    private static final String SQL_READ_BY_CURRENCY_CODES = SqlLoader.loadSqlQuery("exchange-rates.read-by-currency-codes.sql");
    private static final String SQL_UPDATE = SqlLoader.loadSqlQuery("exchange-rates.update.sql");
    private static final String SQL_UPDATE_BY_CURRENCY_CODES = SqlLoader.loadSqlQuery("exchange-rates.update-by-currency-codes.sql");
    private static final String SQL_DELETE = SqlLoader.loadSqlQuery("exchange-rates.delete.sql");

    private final DataSource dataSource;

    public JdbcExchangeRateDao() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    @Override
    public ExchangeRate create(ExchangeRate entity) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_CREATE)) {
            statement.setInt(1, entity.getBaseCurrency().getId());
            statement.setInt(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "The inserted exchange rate already exists: " + entity.getBaseCurrency().getCode() + "/" + entity.getTargetCurrency().getCode());
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error creating exchange rate: " + entity.getBaseCurrency().getCode() + "/" + entity.getTargetCurrency().getCode());
        }

        return readByCurrencyCodes(entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode());
    }

    public ExchangeRate createByCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQL_CREATE_BY_CURRENCY_CODES)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            statement.setBigDecimal(3, rate);

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "Error creating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
            }

            return readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error creating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    @Override
    public ExchangeRate read(int id) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_READ)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Exchange rate with id " + id + " not found");
            }

            return new ExchangeRateResultSetMapper().map(resultSet);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading exchange rate with ID: " + id);
        }
    }

    @Override
    public List<ExchangeRate> readAll() {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSetMapper<ExchangeRate> mapper = new ExchangeRateResultSetMapper();

            while (resultSet.next()) {
                exchangeRates.add(mapper.map(resultSet));
            }

            return exchangeRates;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading all exchange rates");
        }
    }

    @Override
    public ExchangeRate readByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQL_READ_BY_CURRENCY_CODES)) {
            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode + " not found");
            }

            return new ExchangeRateResultSetMapper().map(resultSet);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate entity) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setBigDecimal(1, entity.getRate());
            statement.setInt(2, entity.getId());
            statement.executeUpdate();

            return readByCurrencyCodes(entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode());
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error updating exchange rate with ID: " + entity.getId());
        }
    }

    public ExchangeRate updateByCodes(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setBigDecimal(1, rate);
            statement.setString(2, baseCurrencyCode);
            statement.setString(3, targetCurrencyCode);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode + " not found");
            }

            return readByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error updating exchange rate for " + baseCurrencyCode + "/" + targetCurrencyCode);
        }
    }

    @Override
    public void delete(int id) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Exchange rate with ID " + id + " not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error deleting exchange rate with ID: " + id);
        }
    }
}