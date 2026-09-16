package dto;

public class ExchangeResponse {
    private final String baseCurrencyCode;
    private final String targetCurrencyCode;
    private final float rate;
    private final float amount;
    private final float convertedAmount;

    public ExchangeResponse(String baseCurrencyCode, String targetCurrencyCode, float rate, float amount, float convertedAmount) {
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
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

    public float getAmount() {
        return amount;
    }

    public float getConvertedAmount() {
        return convertedAmount;
    }
}
