package validator;

import exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;

public abstract class RequestValidator {
    public abstract void validate(HttpServletRequest request) throws ValidationException;

    protected void isBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("The required form field '" + fieldName + "' is missing");
        }
    }

    protected void isValidLength(String value, String fieldName, int length) {
        if (value.length() > length) {
            throw new ValidationException("The form field '" + fieldName + "' exceeds the maximum length of " + length);
        }
    }
}
