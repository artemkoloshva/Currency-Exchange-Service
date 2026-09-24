package dto;

public record CurrencyRequest(
        String code,
        String name,
        String sign
) {}
