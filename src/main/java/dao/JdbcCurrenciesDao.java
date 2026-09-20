package dao;

import entity.Currency;
import exception.ConflictException;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import util.SqlLoader;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JdbcCurrenciesDao implements CurrenciesDao {
    private static final String SQL_CREATE = SqlLoader.loadSqlQuery("currencies.create.sql");
    private static final String SQL_READ_BY_CODE = SqlLoader.loadSqlQuery("currencies.read-by-code.sql");
    private static final String SQL_READ = SqlLoader.loadSqlQuery("currencies.read.sql");
    private static final String SQL_READ_ALL = SqlLoader.loadSqlQuery("currencies.read-all.sql");
    private static final String SQL_UPDATE = SqlLoader.loadSqlQuery("currencies.update.sql");
    private static final String SQL_DELETE = SqlLoader.loadSqlQuery("currencies.delete.sql");

    private final Connection connection;

    public JdbcCurrenciesDao() {
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("db.properties"));
            String dbUrl = properties.getProperty("url");

            connection = DriverManager.getConnection(dbUrl);
        } catch (NullPointerException e) {
            throw new NullPointerException(
                    "db.properties not found");
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Failed to connect to the database");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Currency create(Currency entity) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_CREATE)) {
            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getName());
            statement.setString(3, entity.getSign());

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "The inserted currency already exists: " + entity.getCode());
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error creating currency: " + entity.getCode());
        }

        return readByCode(entity.getCode());
    }

    @Override
    public Currency read(int id) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Currency with id " + id + " not found");
            }

            return mapCurrency(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading currency with id: " + id);
        }
    }

    @Override
    public List<Currency> readAll() {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<Currency> currencies = new ArrayList<>();

            while (resultSet.next()) {
                currencies.add(mapCurrency(resultSet));
            }

            return currencies;
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading all currencies");
        }
    }

    @Override
    public Currency update(Currency entity) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getSign());
            statement.setString(3, entity.getCode());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Currency with code " + entity.getCode() + " not found");
            }

            return readByCode(entity.getCode());
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error updating currency with code: " + entity.getCode());
        }
    }

    @Override
    public void delete(int id) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Currency with ID " + id + " not found");
            }
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error deleting currency with id: " + id);
        }
    }

    @Override
    public Currency readByCode(String code) {
        try(PreparedStatement statement = connection.prepareStatement(SQL_READ_BY_CODE)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Currency with code " + code + " not found");
            }

            return mapCurrency(resultSet);
        } catch (SQLException e) {
            throw new InternalServerErrorException(
                    "Error reading currency by code: " + code);
        }
    }

    private Currency mapCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getString("sign")
        );
    }
}
