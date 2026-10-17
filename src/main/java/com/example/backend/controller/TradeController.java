package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.TradeRequest;
import com.example.backend.dto.ApiDtos.TradeResponse;
import com.example.backend.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

	private final TradingService tradingService;

	public TradeController(TradingService tradingService) {
		this.tradingService = tradingService;
	}

	@PostMapping("/buy")
	public TradeResponse buy(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TradeRequest request) {
		return tradingService.buy(jwt.getSubject(), request);
	}

	@PostMapping("/sell")
	public TradeResponse sell(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TradeRequest request) {
		return tradingService.sell(jwt.getSubject(), request);
	}
}
