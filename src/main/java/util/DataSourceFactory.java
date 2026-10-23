package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class DataSourceFactory {
    private DataSourceFactory() {}

    public static HikariDataSource create() {
        Properties props = PropertiesLoader.load("data-source.properties");
        String url = PropertiesLoader.resolveRequired(props, "url", "DB_URL");

        createParentDirectories(url);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("sqlite-pool");

        config.setMaximumPoolSize(PropertiesLoader.getInt(props, "maximumPoolSize", 2));
        config.setMinimumIdle(PropertiesLoader.getInt(props, "minimumIdle", 1));
        config.setConnectionTimeout(PropertiesLoader.getLong(props, "connectionTimeout", 30_000));
        config.setIdleTimeout(PropertiesLoader.getLong(props, "idleTimeout", 600_000));
        config.setMaxLifetime(PropertiesLoader.getLong(props, "maxLifetime", 1_800_000));

        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("busy_timeout", "5000");

        return new HikariDataSource(config);
    }

    private static void createParentDirectories(String url) {
        String prefix = "jdbc:sqlite:";
        if (!url.startsWith(prefix)) return;
        String path = url.substring(prefix.length());
        if (path.startsWith(":memory:") || path.startsWith("file:")) return;

        Path parent = Path.of(path).toAbsolutePath().getParent();
        try {
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create DB directory: " + parent, e);
        }
    }
}