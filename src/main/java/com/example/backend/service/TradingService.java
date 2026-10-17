package com.example.backend.service;

import com.example.backend.dto.ApiDtos.TradeRequest;
import com.example.backend.dto.ApiDtos.TradeResponse;
import com.example.backend.model.AppUser;
import com.example.backend.model.Holding;
import com.example.backend.model.Stock;
import com.example.backend.model.TradeTransaction;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.HoldingRepository;
import com.example.backend.repository.StockRepository;
import com.example.backend.repository.TradeTransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TradingService {

	private final AppUserRepository userRepository;
	private final StockRepository stockRepository;
	private final HoldingRepository holdingRepository;
	private final TradeTransactionRepository transactionRepository;

	public TradingService(AppUserRepository userRepository, StockRepository stockRepository,
			HoldingRepository holdingRepository, TradeTransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.stockRepository = stockRepository;
		this.holdingRepository = holdingRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public TradeResponse buy(String username, TradeRequest request) {
		AppUser user = lockUser(username);
		Stock stock = findStock(request.symbol());
		long quantity = request.quantity();
		BigDecimal total = totalValue(stock.getCurrentPrice(), quantity);

		if (user.getBalance().compareTo(total) < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient virtual balance for this purchase.");
		}

		Holding holding = holdingRepository.findByUser_IdAndStock_Id(user.getId(), stock.getId())
			.orElse(null);
		if (holding == null) {
			holding = new Holding(user, stock, quantity, stock.getCurrentPrice());
		} else {
			long previousQuantity = holding.getQuantity();
			long newQuantity = Math.addExact(previousQuantity, quantity);
			BigDecimal previousCost = holding.getAveragePrice().multiply(BigDecimal.valueOf(previousQuantity));
			BigDecimal purchaseCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
			BigDecimal averagePrice = previousCost.add(purchaseCost)
				.divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
			holding.setQuantity(newQuantity);
			holding.setAveragePrice(averagePrice);
		}

		user.setBalance(user.getBalance().subtract(total));
		holdingRepository.save(holding);
		transactionRepository.save(new TradeTransaction(user, stock, TradeTransaction.Type.BUY, quantity,
			stock.getCurrentPrice(), total));
		return new TradeResponse(stock.getSymbol(), TradeTransaction.Type.BUY, quantity,
			stock.getCurrentPrice(), total, user.getBalance(), "Paper purchase completed; no real order was placed.");
	}

	@Transactional
	public TradeResponse sell(String username, TradeRequest request) {
		AppUser user = lockUser(username);
		Stock stock = findStock(request.symbol());
		long quantity = request.quantity();
		Holding holding = holdingRepository.findByUser_IdAndStock_Id(user.getId(), stock.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
				"You do not own any units of this stock."));

		if (holding.getQuantity() < quantity) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
				"You do not own enough units to complete this sale.");
		}

		BigDecimal total = totalValue(stock.getCurrentPrice(), quantity);
		long remainingQuantity = holding.getQuantity() - quantity;
		if (remainingQuantity == 0) {
			holdingRepository.delete(holding);
		} else {
			holding.setQuantity(remainingQuantity);
		}
		user.setBalance(user.getBalance().add(total));
		transactionRepository.save(new TradeTransaction(user, stock, TradeTransaction.Type.SELL, quantity,
			stock.getCurrentPrice(), total));
		return new TradeResponse(stock.getSymbol(), TradeTransaction.Type.SELL, quantity,
			stock.getCurrentPrice(), total, user.getBalance(), "Paper sale completed; no real order was placed.");
	}

	private AppUser lockUser(String username) {
		return userRepository.findByUsernameForUpdate(username)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
				"The account for this token no longer exists."));
	}

	private Stock findStock(String symbol) {
		String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
		return stockRepository.findBySymbolIgnoreCase(normalizedSymbol)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock symbol was not found."));
	}

	private BigDecimal totalValue(BigDecimal price, long quantity) {
		return price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.UNNECESSARY);
	}
}
