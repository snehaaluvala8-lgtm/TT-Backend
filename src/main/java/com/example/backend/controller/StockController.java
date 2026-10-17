package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.StockResponse;
import com.example.backend.service.StockService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		this.stockService = stockService;
	}

	@GetMapping
	public List<StockResponse> findAll() {
		return stockService.findAll();
	}

	@GetMapping("/{symbol}")
	public StockResponse findBySymbol(@PathVariable String symbol) {
		return stockService.findBySymbol(symbol);
	}
}
