package db;

import util.SqlLoader;

import javax.sql.DataSource;
import java.sql.*;

public class DatabaseInitializer {
    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        String schema = SqlLoader.loadSqlQuery("schema.sql");
        String seed = SqlLoader.loadSqlQuery("seed.sql");

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                execute(statement, schema);
                if (isEmpty(statement, "currencies")) {
                    execute(statement, seed);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private boolean isEmpty(Statement statement, String table) throws SQLException {
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void execute(Statement statement, String script) throws SQLException {
        for (String sql : script.split(";")) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty()) {
                statement.execute(trimmed);
            }
        }
    }
}