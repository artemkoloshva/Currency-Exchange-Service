package dto;

public class ExchangeRateResponse {
    private final int id;
    private final CurrencyResponse baseCurrency;
    private final CurrencyResponse targetCurrency;
    private final float rate;

    public ExchangeRateResponse(int id, String baseCurrencyCode, String targetCurrencyCode, float rate) {
        this(id,
                new CurrencyResponse(0, baseCurrencyCode, baseCurrencyCode, ""),
                new CurrencyResponse(0, targetCurrencyCode, targetCurrencyCode, ""),
                rate);
    }

    public ExchangeRateResponse(int id, CurrencyResponse baseCurrency, CurrencyResponse targetCurrency, float rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public CurrencyResponse getBaseCurrency() {
        return baseCurrency;
    }

    public CurrencyResponse getTargetCurrency() {
        return targetCurrency;
    }

    public String getBaseCurrencyCode() {
        return baseCurrency != null ? baseCurrency.getCode() : null;
    }

    public String getTargetCurrencyCode() {
        return targetCurrency != null ? targetCurrency.getCode() : null;
    }

    public float getRate() {
        return rate;
    }
}
