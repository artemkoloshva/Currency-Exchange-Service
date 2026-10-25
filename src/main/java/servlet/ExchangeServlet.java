package servlet;

import dto.ExchangeRequest;
import dto.ExchangeResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import parser.BigDecimalParser;
import service.ExchangeService;
import validator.CurrencyValidator;
import validator.ExchangeRateValidator;

import java.io.IOException;
import java.math.BigDecimal;

public class ExchangeServlet extends AbstractJsonServlet {
    private final ExchangeService exchangeService;
    private final CurrencyValidator currencyValidator;
    private final ExchangeRateValidator exchangeRateValidator;
    private final BigDecimalParser bigDecimalParser;

    public ExchangeServlet(ExchangeService service, CurrencyValidator currencyValidator, ExchangeRateValidator exchangeRateValidator, BigDecimalParser bigDecimalParser) {
        this.exchangeService = service;
        this.currencyValidator = currencyValidator;
        this.exchangeRateValidator = exchangeRateValidator;
        this.bigDecimalParser = bigDecimalParser;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amount = request.getParameter("amount");

        currencyValidator.validateCode(from);
        currencyValidator.validateCode(to);
        exchangeRateValidator.validateRate(amount);

        BigDecimal parsedAmount = bigDecimalParser.parse(amount);

        ExchangeRequest exchangeRequest = new ExchangeRequest(from.toUpperCase(), to.toUpperCase(), parsedAmount);
        ExchangeResponse exchangeResponse = exchangeService.exchange(exchangeRequest);
        writeJson(response, HttpServletResponse.SC_OK, exchangeResponse);
    }
}
