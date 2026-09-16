package dto;

public class CurrencyRequest {
    private final String code;
    private final String name;
    private final String sign;

    public CurrencyRequest(String code, String name, String sign) {
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return name;
    }

    public String getSign() {
        return sign;
    }
}
