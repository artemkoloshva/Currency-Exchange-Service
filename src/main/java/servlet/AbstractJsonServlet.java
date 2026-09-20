package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public abstract class AbstractJsonServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    protected <T> T readJsonBody(HttpServletRequest request, Class<T> type) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (Reader reader = request.getReader()) {
            T body = GSON.fromJson(reader, type);

            if (body == null) {
                throw new IllegalArgumentException("Request body is empty");
            }

            return body;
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    protected void writeJson(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        GSON.toJson(payload, response.getWriter());
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, new ErrorResponse(message));
    }

    protected String getFormParameter(HttpServletRequest request, String parameterName) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        String bodyString = body.toString().trim();
        if (bodyString.isEmpty()) {
            return null;
        }

        String[] pairs = bodyString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                if (parameterName.equals(key)) {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
        }

        return null;
    }

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    protected record ErrorResponse(String message) {}
}
