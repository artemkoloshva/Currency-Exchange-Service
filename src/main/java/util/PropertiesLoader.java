package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesLoader {

    private PropertiesLoader() {}

    public static Properties load(String resourceName) {
        Properties properties = new Properties();
        try (InputStream is = PropertiesLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found in classpath: " + resourceName);
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties: " + resourceName, e);
        }
        return properties;
    }

    public static String resolveRequired(Properties props, String propsKey, String envKey) {
        String value = resolve(props, propsKey, envKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required config: set env variable '" + envKey
                            + "' or property '" + propsKey + "'"
            );
        }
        return value;
    }

    public static String resolve(Properties props, String propsKey, String envKey, String defaultValue) {
        String value = resolve(props, propsKey, envKey);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static String resolve(Properties props, String propsKey, String envKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromSysProp = System.getProperty(envKey);
        if (fromSysProp != null && !fromSysProp.isBlank()) {
            return fromSysProp;
        }
        return props.getProperty(propsKey);
    }

    public static int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static long getLong(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }
}