package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SqlLoader {
    private static final String SQL_DIR = "sql/";

    public static String loadSqlQuery(String fileName) {
        String path = SQL_DIR + fileName;

        try (InputStream in = SqlLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("SQL file not found in classpath: " + path);
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error loading SQL query from file: " + fileName, e);
        }
    }
}
