package servlet;

import dto.CurrencyRequest;
import dto.CurrencyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import validator.CurrencyValidator;

import java.io.IOException;
import java.util.List;

public class CurrenciesServlet extends AbstractJsonServlet {
    private final CurrencyService currencyService;
    private final CurrencyValidator currencyValidator;

    public CurrenciesServlet(CurrencyService currencyService, CurrencyValidator currencyValidator) {
        this.currencyService = currencyService;
        this.currencyValidator = currencyValidator;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<CurrencyResponse> currencyResponses = currencyService.getAllCurrencies();
        writeJson(response, HttpServletResponse.SC_OK, currencyResponses);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        currencyValidator.validate(request);

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        CurrencyRequest currencyRequest = new CurrencyRequest(
                name,
                code.toUpperCase(),
                sign
        );
        CurrencyResponse currencyResponse = currencyService.addCurrency(currencyRequest);
        writeJson(response, HttpServletResponse.SC_CREATED, currencyResponse);
    }
}
