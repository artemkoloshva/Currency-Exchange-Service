package validator;

import exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import parser.BigDecimalParser;

import java.math.BigDecimal;

public class ExchangeRateValidator extends RequestValidator {
    private CurrencyValidator currencyValidator;
    private BigDecimalParser bigDecimalParser;

    public ExchangeRateValidator(CurrencyValidator currencyValidator, BigDecimalParser bigDecimalParser) {
        this.currencyValidator = currencyValidator;
        this.bigDecimalParser = bigDecimalParser;
    }

    @Override
    public void validate(HttpServletRequest request) throws ValidationException {
        currencyValidator.validateCode(request.getParameter("targetCurrencyCode"));
        currencyValidator.validateCode(request.getParameter("baseCurrencyCode"));
        validateRate(request.getParameter("rate"));
    }

    public void validatePair(String pair) {
        isBlank(pair, "pair");
        isValidLength(pair, "pair", 6);
        currencyValidator.validateCode(pair.substring(0, 3));
        currencyValidator.validateCode(pair.substring(3));
    }

    public void validateRate(String value) {
        isBlank(value, "rate");
        
        BigDecimal rate = bigDecimalParser.parse(value);

        if (rate.signum() <= 0) {
            throw new ValidationException("The form field 'rate' must be a positive number");
        }
    }
}
