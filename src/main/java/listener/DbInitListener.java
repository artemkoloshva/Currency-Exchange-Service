package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Collectors;
import java.sql.ResultSet;

@WebListener
public class DbInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.sqlite.JDBC");

            Properties properties = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("ds.properties")) {
                if (in == null) {
                    throw new IllegalStateException("ds.properties not found");
                }
                properties.load(in);
            }

            String databaseUrl = properties.getProperty("url");
            if (databaseUrl == null || databaseUrl.isBlank()) {
                throw new IllegalStateException("database URL is missing in ds.properties");
            }

            String schemaSql = readResourceSql("sql/schema.sql");
            String seedSql = readResourceSql("sql/seed.sql");

            try (Connection connection = DriverManager.getConnection(databaseUrl);
                 Statement statement = connection.createStatement()) {

                statement.execute("PRAGMA foreign_keys = ON");

                connection.setAutoCommit(false);
                try {
                    executeSqlScript(statement, schemaSql);

                    boolean currenciesEmpty;
                    try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM currencies")) {
                        resultSet.next();
                        currenciesEmpty = resultSet.getInt(1) == 0;
                    }

                    if (currenciesEmpty) {
                        executeSqlScript(statement, seedSql);
                    }

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite driver not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read DB initialization files", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private String readResourceSql(String resourcePath) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException(resourcePath + " not found");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private void executeSqlScript(Statement statement, String sqlScript) throws SQLException {
        for (String sql : sqlScript.split(";")) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty()) {
                statement.execute(trimmed);
            }
        }
    }
}
