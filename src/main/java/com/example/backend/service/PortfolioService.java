package com.example.backend.service;

import com.example.backend.dto.ApiDtos.HoldingResponse;
import com.example.backend.dto.ApiDtos.PortfolioResponse;
import com.example.backend.model.AppUser;
import com.example.backend.model.Holding;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.HoldingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PortfolioService {

	private final AppUserRepository userRepository;
	private final HoldingRepository holdingRepository;

	public PortfolioService(AppUserRepository userRepository, HoldingRepository holdingRepository) {
		this.userRepository = userRepository;
		this.holdingRepository = holdingRepository;
	}

	@Transactional(readOnly = true)
	public PortfolioResponse getPortfolio(String username) {
		AppUser user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
				"The account for this token no longer exists."));

		var holdings = holdingRepository.findAllByUser_Id(user.getId()).stream()
			.sorted(Comparator.comparing(holding -> holding.getStock().getSymbol()))
			.map(this::toHoldingResponse)
			.toList();
		BigDecimal holdingsValue = holdings.stream()
			.map(HoldingResponse::holdingValue)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(2, RoundingMode.HALF_UP);
		BigDecimal unrealizedProfitLoss = holdings.stream()
			.map(HoldingResponse::unrealizedProfitLoss)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(2, RoundingMode.HALF_UP);

		return new PortfolioResponse(user.getBalance(), holdingsValue, user.getBalance().add(holdingsValue),
			unrealizedProfitLoss, holdings);
	}

	private HoldingResponse toHoldingResponse(Holding holding) {
		BigDecimal holdingValue = holding.getStock().getCurrentPrice()
			.multiply(BigDecimal.valueOf(holding.getQuantity()));
		BigDecimal costBasis = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
		return new HoldingResponse(holding.getStock().getSymbol(), holding.getStock().getCompanyName(),
			holding.getQuantity(), holding.getAveragePrice(), holding.getStock().getCurrentPrice(),
			holdingValue.setScale(2, RoundingMode.HALF_UP),
			holdingValue.subtract(costBasis).setScale(2, RoundingMode.HALF_UP));
	}
}
