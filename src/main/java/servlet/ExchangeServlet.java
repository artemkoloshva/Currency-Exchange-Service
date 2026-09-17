package servlet;

import dao.JdbcCurrenciesDao;
import dao.JdbcExchangeRatesDao;
import dto.ExchangeRequest;
import dto.ExchangeResponse;
import exception.InternalServerErrorException;
import exception.NotFoundException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DefaultExchangeService;
import service.ExchangeService;

import java.io.IOException;

@WebServlet("/exchange")
public class ExchangeServlet extends AbstractJsonServlet {
    private final ExchangeService exchangeService = new DefaultExchangeService(new JdbcCurrenciesDao(), new JdbcExchangeRatesDao());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String from = request.getParameter("from").toUpperCase();
            String to = request.getParameter("to").toUpperCase();
            String amount = request.getParameter("amount");

            if (isBlank(from) || isBlank(to) || isBlank(amount)) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The required query parameters are missing");
                return;
            }

            float parsedAmount;

            try {
                parsedAmount = Float.parseFloat(amount.trim());
            } catch (NumberFormatException e) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrectly entered amount");
                return;
            }

            ExchangeRequest exchangeRequest = new ExchangeRequest(from.trim().toUpperCase(), to.trim().toUpperCase(), parsedAmount);
            ExchangeResponse exchangeResponse = exchangeService.exchange(exchangeRequest);

            writeJson(response, HttpServletResponse.SC_OK, exchangeResponse);
        } catch (NotFoundException e) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }  catch (InternalServerErrorException e) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
