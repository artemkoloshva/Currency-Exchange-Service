package servlet;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import exception.ConflictException;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DefaultExchangeRateService;
import service.ExchangeRateService;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends AbstractJsonServlet {
    private static final ExchangeRateService exchangeRatesService = new DefaultExchangeRateService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<ExchangeRateResponse> exchangeRateResponses = exchangeRatesService.getAllExchangeRates();
            writeJson(response, HttpServletResponse.SC_OK, exchangeRateResponses);
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to load exchange rate");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
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

            ExchangeRateRequest exchangeRateRequest = new ExchangeRateRequest(
                    baseCurrencyCode, targetCurrencyCode, floatRate);
            ExchangeRateResponse exchangeRateResponse = exchangeRatesService.addExchangeRate(exchangeRateRequest);

            writeJson(response, HttpServletResponse.SC_CREATED, exchangeRateResponse);
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrectly entered rate");
        } catch (ConflictException e) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "A currency pair with this code already exists");
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "One (or both) currency from a currency pair does not exist in the database");
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to load exchange rate");
        }
    }
}
