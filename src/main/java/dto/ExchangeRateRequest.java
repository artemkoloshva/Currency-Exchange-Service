package dto;

import java.math.BigDecimal;

public record ExchangeRateRequest(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal rate
) {}
