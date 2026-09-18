package dto;

public class ExchangeRateResponse {
    private final int id;
    private final CurrencyResponse baseCurrency;
    private final CurrencyResponse targetCurrency;
    private final float rate;

    public ExchangeRateResponse(int id, CurrencyResponse base, CurrencyResponse target, float rate) {
        this.id = id;
        this.baseCurrency = base;
        this.targetCurrency = target;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public CurrencyResponse getBaseCurrency() {
        return baseCurrency;
    }

    public CurrencyResponse getTarget() {
        return targetCurrency;
    }

    public float getRate() {
        return rate;
    }
}
