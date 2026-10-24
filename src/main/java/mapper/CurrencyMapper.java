package mapper;

import dto.CurrencyRequest;
import dto.CurrencyResponse;
import entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface CurrencyMapper {
    CurrencyResponse toResponse(Currency currency);

    List<CurrencyResponse> toResponseList(List<Currency> currencies);

    @Mapping(target = "id", constant = "0")
    Currency toEntity(CurrencyRequest request);
}
