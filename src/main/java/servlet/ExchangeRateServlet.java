package servlet;

import dao.ExchangeRatesDao;
import dao.JdbcExchangeRatesDao;
import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DefaultExchangeRateService;
import service.ExchangeRateService;

import java.io.IOException;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends AbstractJsonServlet {
    private final ExchangeRateService exchangeRateService = new DefaultExchangeRateService(new JdbcExchangeRatesDao());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (isBlank(pathInfo) || pathInfo.equals("/")) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The currency codes of the pair are missing in the address");
                return;
            }

            String code = pathInfo.substring(1).toUpperCase();

            if (code.length() != 6) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrect exchange rate");
                return;
            }

            ExchangeRateResponse exchangeRateResponse = exchangeRateService.getExchangeRateByCodes(
                    code.substring(0, 3), code.substring(3));
            writeJson(response, HttpServletResponse.SC_OK, exchangeRateResponse);
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            if (isBlank(pathInfo) || pathInfo.equals("/")) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The currency codes of the pair are missing in the address");
                return;
            }

            String code = pathInfo.substring(1).toUpperCase();

            if (code.length() != 6) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrect exchange rate");
                return;
            }

            String rate = request.getParameter("rate");

            if (isBlank(rate)) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The required form field is missing");
                return;
            }

            float floatRate;

            try {
                floatRate = Float.parseFloat(rate.trim());
            } catch (NumberFormatException e) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrectly entered rate");
                return;
            }

            ExchangeRateRequest exchangeRateRequest = new ExchangeRateRequest(code.substring(0, 3), code.substring(3), floatRate);
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.updateExchangeRate(exchangeRateRequest);

            writeJson(response, HttpServletResponse.SC_OK, exchangeRateResponse);
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();

        if ("PATCH".equalsIgnoreCase(method)) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }
}
