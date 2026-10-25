package validator;

import exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;

public class CurrencyValidator extends RequestValidator {
    private final static String NAME_FIELD = "name";
    private final static String CODE_FIELD = "code";
    private final static String SIGN_FIELD = "sign";
    private final static int MAX_LENGTH = 3;

    @Override
    public void validate(HttpServletRequest request) throws ValidationException {
        validateName(request.getParameter(NAME_FIELD));
        validateCode(request.getParameter(CODE_FIELD));
        validateSign(request.getParameter(SIGN_FIELD));
    }

    public void validateName(String name) {
        isBlank(name, NAME_FIELD);
    }

    public void validateCode(String code) {
        isBlank(code, CODE_FIELD);
        isValidLength(code, CODE_FIELD, MAX_LENGTH);
    }

    public void validateSign(String sign) {
        isBlank(sign, SIGN_FIELD);
        isValidLength(sign, SIGN_FIELD, MAX_LENGTH);
    }
}
