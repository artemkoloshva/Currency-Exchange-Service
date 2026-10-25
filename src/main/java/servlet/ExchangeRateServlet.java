package servlet;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import parser.BigDecimalParser;
import service.ExchangeRateService;
import validator.ExchangeRateValidator;

import java.io.IOException;
import java.math.BigDecimal;

public class ExchangeRateServlet extends AbstractJsonServlet {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateValidator exchangeRateValidator;
    private final BigDecimalParser bigDecimalParser;

    public ExchangeRateServlet(ExchangeRateService exchangeRateService, ExchangeRateValidator exchangeRateValidator, BigDecimalParser bigDecimalParser) {
        this.exchangeRateService = exchangeRateService;
        this.exchangeRateValidator = exchangeRateValidator;
        this.bigDecimalParser = bigDecimalParser;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pair = extractPair(request);
        exchangeRateValidator.validatePair(pair);
        ExchangeRateResponse result = exchangeRateService.getExchangeRateByCodes(pair.substring(0, 3), pair.substring(3));
        writeJson(response, HttpServletResponse.SC_OK, result);
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pair = extractPair(request);
        exchangeRateValidator.validatePair(pair);

        String rateValue = getFormParameter(request, "rate");
        exchangeRateValidator.validateRate(rateValue);
        BigDecimal rate = bigDecimalParser.parse(rateValue);

        ExchangeRateRequest dto = new ExchangeRateRequest(pair.substring(0, 3), pair.substring(3), rate);
        ExchangeRateResponse result = exchangeRateService.updateExchangeRate(dto);
        writeJson(response, HttpServletResponse.SC_OK, result);
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

    private String extractPair(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        String pair = (pathInfo == null || pathInfo.length() <= 1)
                ? null
                : pathInfo.substring(1).toUpperCase();
        exchangeRateValidator.validatePair(pair);
        return pair;
    }
}
