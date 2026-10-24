package mapper;

import dto.ExchangeRateRequest;
import dto.ExchangeRateResponse;
import entity.Currency;
import entity.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface ExchangeRateMapper {
    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponseList(List<ExchangeRate> exchangeRates);

    @Mapping(target = "id", constant = "0")
    @Mapping(target = "baseCurrency", source = "baseCurrency")
    @Mapping(target = "targetCurrency", source = "targetCurrency")
    @Mapping(target = "rate", source = "request.rate")
    ExchangeRate toEntity(ExchangeRateRequest request,
                          Currency baseCurrency,
                          Currency targetCurrency);
}
