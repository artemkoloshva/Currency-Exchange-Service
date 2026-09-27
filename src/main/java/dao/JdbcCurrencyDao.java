package dao;

import entity.Currency;
import exception.ConflictException;
import exception.DatabaseException;
import exception.NotFoundException;
import mapper.CurrencyResultSetMapper;
import mapper.ResultSetMapper;
import util.DataSourceProvider;
import util.SqlLoader;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCurrencyDao implements CurrencyDao {
    private static final String SQL_CREATE = SqlLoader.loadSqlQuery("currencies.create.sql");
    private static final String SQL_READ = SqlLoader.loadSqlQuery("currencies.read.sql");
    private static final String SQL_READ_ALL = SqlLoader.loadSqlQuery("currencies.read-all.sql");
    private static final String SQL_READ_BY_CODE = SqlLoader.loadSqlQuery("currencies.read-by-code.sql");
    private static final String SQL_UPDATE = SqlLoader.loadSqlQuery("currencies.update.sql");
    private static final String SQL_UPDATE_BY_CODE = SqlLoader.loadSqlQuery("currencies.update-by-code.sql");
    private static final String SQL_DELETE = SqlLoader.loadSqlQuery("currencies.delete.sql");

    private final DataSource dataSource;

    public JdbcCurrencyDao() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    @Override
    public Currency create(Currency entity) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_CREATE)) {
            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getName());
            statement.setString(3, entity.getSign());

            if (statement.executeUpdate() == 0) {
                throw new ConflictException(
                        "The inserted currency already exists: " + entity.getCode());
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error creating currency: " + entity.getCode());
        }

        return readByCode(entity.getCode());
    }

    @Override
    public Currency read(int id) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_READ)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Currency with id " + id + " not found");
            }

            return new CurrencyResultSetMapper().map(resultSet);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading currency with id: " + id);
        }
    }

    @Override
    public List<Currency> readAll() {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_READ_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            ResultSetMapper<Currency> mapper = new CurrencyResultSetMapper();

            while (resultSet.next()) {
                currencies.add(mapper.map(resultSet));
            }

            return currencies;
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading all currencies");
        }
    }

    @Override
    public Currency readByCode(String code) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = dataSource.getConnection().prepareStatement(SQL_READ_BY_CODE)) {
            statement.setString(1, code);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException(
                        "Currency with code " + code + " not found");
            }

            return new CurrencyResultSetMapper().map(resultSet);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error reading currency by code: " + code);
        }
    }

    @Override
    public Currency update(Currency entity) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getSign());
            statement.setInt(3, entity.getId());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Currency with id " + entity.getId() + " not found");
            }

            return readByCode(entity.getCode());
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error updating currency with id: " + entity.getId());
        }
    }

    @Override
    public Currency updateByCode(Currency entity) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_CODE)) {
            statement.setString(1, entity.getName());
            statement.setString(2, entity.getSign());
            statement.setString(3, entity.getCode());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Currency with code " + entity.getCode() + " not found");
            }

            return readByCode(entity.getCode());
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error updating currency by code: " + entity.getCode());
        }
    }

    @Override
    public void delete(int id) {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
            statement.setInt(1, id);

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException(
                        "Currency with ID " + id + " not found");
            }
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Error deleting currency with id: " + id);
        }
    }
}
