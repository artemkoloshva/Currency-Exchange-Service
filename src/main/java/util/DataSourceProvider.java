package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Properties;

public final class DataSourceProvider {

    private static final String CONFIG_FILE = "db.properties";
    private static final HikariDataSource DATA_SOURCE;

    static {
        Properties props = PropertiesLoader.load(CONFIG_FILE);
        DATA_SOURCE = createDataSource(props);
        Runtime.getRuntime().addShutdownHook(new Thread(DATA_SOURCE::close));
    }

    private DataSourceProvider() {}

    private static HikariDataSource createDataSource(Properties properties) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(PropertiesLoader.resolveRequired(properties, "url", "DB_URL"));
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(PropertiesLoader.getInt(properties, "maximumPoolSize", 2));
        config.setMinimumIdle(PropertiesLoader.getInt(properties, "minimumIdle", 1));
        config.setConnectionTimeout(PropertiesLoader.getLong(properties, "connectionTimeout", 30_000));
        config.setIdleTimeout(PropertiesLoader.getLong(properties, "idleTimeout", 600_000));
        config.setMaxLifetime(PropertiesLoader.getLong(properties, "maxLifetime", 1_800_000));

        config.setPoolName("sqlite-pool");
        config.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA busy_timeout=5000;");

        return new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return DATA_SOURCE;
    }
}
