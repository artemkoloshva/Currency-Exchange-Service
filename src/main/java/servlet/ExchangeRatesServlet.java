package servlet;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import parser.BigDecimalParser;
import service.ExchangeRateService;
import validator.ExchangeRateValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends AbstractJsonServlet {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateValidator exchangeRateValidator;
    private final BigDecimalParser bigDecimalParser;

    public ExchangeRatesServlet(ExchangeRateService service, ExchangeRateValidator exchangeRateValidator, BigDecimalParser bigDecimalParser) {
        this.exchangeRateService = service;
        this.exchangeRateValidator = exchangeRateValidator;
        this.bigDecimalParser = bigDecimalParser;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ExchangeRateResponse> exchangeRateResponses = exchangeRateService.getAllExchangeRates();
        writeJson(response, HttpServletResponse.SC_OK, exchangeRateResponses);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        exchangeRateValidator.validate(request);

        String baseCurrencyCode = request.getParameter("baseCurrencyCode").toUpperCase();
        String targetCurrencyCode = request.getParameter("targetCurrencyCode").toUpperCase();
        BigDecimal rate = bigDecimalParser.parse(request.getParameter("rate"));

        ExchangeRateRequest exchangeRateRequest = new ExchangeRateRequest(baseCurrencyCode, targetCurrencyCode, rate);
        ExchangeRateResponse exchangeRateResponse = exchangeRateService.addExchangeRate(exchangeRateRequest);
        writeJson(response, HttpServletResponse.SC_CREATED, exchangeRateResponse);
    }
}
