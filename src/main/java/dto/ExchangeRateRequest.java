package dto;

public class ExchangeRateRequest {
    private final String baseCurrencyCode;
    private final String targetCurrencyCode;
    private final float rate;

    public ExchangeRateRequest(String baseCurrencyCode, String targetCurrencyCode, float rate) {
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.rate = rate;
    }

    public String getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public String getTargetCurrencyCode() {
        return targetCurrencyCode;
    }

    public float getRate() {
        return rate;
    }
}
