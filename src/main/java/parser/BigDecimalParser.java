package parser;

import exception.ValidationException;

import java.math.BigDecimal;

public class BigDecimalParser {
    public static BigDecimal parse(String value) {
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidationException("The value must be a valid number");
        }
    }
}
