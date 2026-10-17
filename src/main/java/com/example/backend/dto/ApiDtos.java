package com.example.backend.dto;

import com.example.backend.model.TradeTransaction;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ApiDtos {

	private ApiDtos() {
	}

	public record RegisterRequest(
		@NotBlank @Size(min = 3, max = 30) String username,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 72) String password
	) {
	}

	public record LoginRequest(
		@NotBlank @Size(max = 254) String identifier,
		@NotBlank @Size(max = 72) String password
	) {
	}

	public record AuthResponse(String username) {
	}

	public record StockResponse(String symbol, String companyName, BigDecimal currentPrice, String priceType) {
	}

	public record TradeRequest(
		@NotBlank @Size(max = 12) String symbol,
		@Positive long quantity
	) {
	}

	public record TradeResponse(
		String symbol,
		TradeTransaction.Type type,
		long quantity,
		BigDecimal executionPrice,
		BigDecimal totalValue,
		BigDecimal availableBalance,
		String message
	) {
	}

	public record HoldingResponse(
		String symbol,
		String companyName,
		long quantity,
		BigDecimal averagePurchasePrice,
		BigDecimal currentPrice,
		BigDecimal holdingValue,
		BigDecimal unrealizedProfitLoss
	) {
	}

	public record PortfolioResponse(
		BigDecimal availableBalance,
		BigDecimal totalHoldingsValue,
		BigDecimal totalPortfolioValue,
		BigDecimal unrealizedProfitLoss,
		List<HoldingResponse> holdings
	) {
	}

	public record TransactionResponse(
		String symbol,
		TradeTransaction.Type type,
		long quantity,
		BigDecimal executionPrice,
		BigDecimal totalValue,
		Instant createdAt
	) {
	}

	public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		Map<String, String> validationErrors
	) {
	}
}
