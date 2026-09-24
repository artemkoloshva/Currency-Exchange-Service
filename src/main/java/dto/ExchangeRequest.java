package dto;

import java.math.BigDecimal;

public record ExchangeRequest(
        String baseCurrencyCode,
        String targetCurrencyCode,
        BigDecimal amount
) {}
