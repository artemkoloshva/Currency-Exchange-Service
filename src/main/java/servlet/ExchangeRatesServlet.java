package servlet;

import dao.JdbcExchangeRatesDao;
import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import exception.ConflictException;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DefaultExchangeService;
import service.ExchangeService;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends AbstractJsonServlet {
    private final ExchangeService exchangeRateService = new DefaultExchangeService(new JdbcExchangeRatesDao());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<ExchangeRateResponse> exchangeRateResponses = exchangeRateService.getAllExchangeRates();
            writeJson(response, HttpServletResponse.SC_OK, exchangeRateResponses);
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode").toUpperCase();
            String targetCurrencyCode = request.getParameter("targetCurrencyCode").toUpperCase();
            String rate = request.getParameter("rate");

            if (isBlank(baseCurrencyCode) || isBlank(targetCurrencyCode) || isBlank(rate)) {
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

            ExchangeRateRequest exchangeRateRequest = new ExchangeRateRequest(baseCurrencyCode, targetCurrencyCode, floatRate);
            ExchangeRateResponse exchangeRateResponse = exchangeRateService.addExchangeRate(exchangeRateRequest);

            writeJson(response, HttpServletResponse.SC_CREATED, exchangeRateResponse);
        } catch (ConflictException e) {
            writeError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
