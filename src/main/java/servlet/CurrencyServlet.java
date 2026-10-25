package servlet;

import dto.CurrencyResponse;
import exception.NotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import validator.CurrencyValidator;
import validator.RequestValidator;

import java.io.IOException;

@WebServlet("/currency/*")
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
