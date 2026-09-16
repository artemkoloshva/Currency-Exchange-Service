package servlet;

import dto.CurrencyRequest;
import dto.CurrencyResponse;
import exception.BadRequestException;
import exception.ConflictException;
import exception.InternalServerErrorException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import service.DefaultCurrencyService;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends AbstractJsonServlet {
    private final CurrencyService currencyService = new DefaultCurrencyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<CurrencyResponse> currencyResponses = currencyService.getAllCurrencies();
            writeJson(response, HttpServletResponse.SC_OK, currencyResponses);
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to load currencies");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String name = request.getParameter("name");
            String code = request.getParameter("code");
            String sign = request.getParameter("sign");

            if (isBlank(name) || isBlank(code) || isBlank(sign)) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The required form field is missing");
                return;
            }

            CurrencyRequest currencyRequest = new CurrencyRequest(code.trim(), name.trim(), sign.trim());
            CurrencyResponse currencyResponse = currencyService.addCurrency(currencyRequest);

            writeJson(response, HttpServletResponse.SC_CREATED, currencyResponse);
        } catch (ConflictException e) {
            writeError(response, HttpServletResponse.SC_CONFLICT,
                    "Currency with this code already exists");
        } catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to add currency");
        }
    }
}
