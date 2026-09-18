package dto;

import entity.Currency;

public class ExchangeResponse {
    private final CurrencyResponse baseCurrency;
    private final CurrencyResponse targetCurrency;
    private final float rate;
    private final float amount;
    private final float convertedAmount;

    public ExchangeResponse(CurrencyResponse baseCurrency, CurrencyResponse targetCurrency, float rate, float amount, float convertedAmount) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }

    public CurrencyResponse getBaseCurrency() {
        return baseCurrency;
    }

    public CurrencyResponse getTargetCurrency() {
        return targetCurrency;
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
