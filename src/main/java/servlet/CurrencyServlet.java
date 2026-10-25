package servlet;

import dto.CurrencyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import validator.CurrencyValidator;

import java.io.IOException;

public class CurrencyServlet extends AbstractJsonServlet {
    private final CurrencyService currencyService;
    private final CurrencyValidator currencyValidator;

    public CurrencyServlet(CurrencyService currencyService, CurrencyValidator currencyValidator) {
        this.currencyService = currencyService;
        this.currencyValidator = currencyValidator;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String code = (pathInfo == null || pathInfo.length() <= 1)
                ? null
                : pathInfo.substring(1);

        currencyValidator.validateCode(code);

        CurrencyResponse currencyResponse = currencyService.getCurrencyByCode(code.toUpperCase());
        writeJson(response, HttpServletResponse.SC_OK, currencyResponse);
    }
}
