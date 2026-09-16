package model;

public class ExchangeRate {
    private final int id;
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private final float rate;

    public ExchangeRate(int id, Currency baseCurrency, Currency targetCurrency, float rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public float getRate() {
        return rate;
    }
}
