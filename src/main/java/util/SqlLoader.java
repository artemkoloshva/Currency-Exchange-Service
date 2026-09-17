package util;

public class SqlLoader {
    private static final String SQL_FILE_PATH = "src/main/resources/sql/";

    public static String loadSqlQuery(String fileName) {
        StringBuilder sqlQuery = new StringBuilder();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(SQL_FILE_PATH + fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                sqlQuery.append(line).append("\n");
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error loading SQL query from file: " + fileName, e);
        }

        return sqlQuery.toString();
    }
}
