package servlet;

import dto.CurrencyResponse;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import service.DefaultCurrencyService;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends AbstractJsonServlet {
    private final CurrencyService currencyService = new DefaultCurrencyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (isBlank(pathInfo) || pathInfo.equals("/")) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The currency code is missing from the address");
                return;
            }

            String code = pathInfo.substring(1);
            CurrencyResponse currencyResponse = currencyService.getCurrencyByCode(code);

            writeJson(response, HttpServletResponse.SC_OK, currencyResponse);
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
