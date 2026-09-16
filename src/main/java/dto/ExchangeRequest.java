package dto;

public class ExchangeRequest {
    private final String baseCurrencyCode;
    private final String targetCurrencyCode;
    private final float amount;

    public ExchangeRequest(String baseCurrencyCode, String targetCurrencyCode, float amount) {
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.amount = amount;
    }

    public String getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }

    public float getAmount() {
        return amount;
    }
}
