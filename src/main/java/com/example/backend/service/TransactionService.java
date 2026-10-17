package com.example.backend.service;

import com.example.backend.dto.ApiDtos.TransactionResponse;
import com.example.backend.model.TradeTransaction;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.TradeTransactionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {

	private final AppUserRepository userRepository;
	private final TradeTransactionRepository transactionRepository;

	public TransactionService(AppUserRepository userRepository, TradeTransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> findForUser(String username) {
		Long userId = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(username, username)
			.map(user -> user.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
				"The account for this token no longer exists."));
		return transactionRepository.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
			.map(this::toResponse)
			.toList();
	}

	private TransactionResponse toResponse(TradeTransaction transaction) {
		return new TransactionResponse(transaction.getStock().getSymbol(), transaction.getType(),
			transaction.getQuantity(), transaction.getPrice(), transaction.getTotalValue(),
			transaction.getCreatedAt());
	}
}
