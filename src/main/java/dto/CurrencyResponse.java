package dto;

public record CurrencyResponse(
        Integer id,
        String code,
        String name,
        String sign
) {}
