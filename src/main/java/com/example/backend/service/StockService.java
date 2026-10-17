package com.example.backend.service;

import com.example.backend.dto.ApiDtos.StockResponse;
import com.example.backend.model.Stock;
import com.example.backend.repository.StockRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StockService {

	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Transactional(readOnly = true)
	public List<StockResponse> findAll() {
		return stockRepository.findAllByOrderBySymbolAsc().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public StockResponse findBySymbol(String symbol) {
		String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
		Stock stock = stockRepository.findBySymbolIgnoreCase(normalizedSymbol)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock symbol was not found."));
		return toResponse(stock);
	}

	StockResponse toResponse(Stock stock) {
		return new StockResponse(stock.getSymbol(), stock.getCompanyName(), stock.getCurrentPrice(),
			"Simulated educational price; not a real-time quote.");
	}
}
