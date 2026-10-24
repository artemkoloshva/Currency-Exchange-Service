package service;

import dto.ExchangeRequest;
import dto.ExchangeResponse;

public interface ExchangeService {
    ExchangeResponse exchange(ExchangeRequest request);
}
