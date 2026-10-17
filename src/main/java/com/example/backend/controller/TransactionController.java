package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.TransactionResponse;
import com.example.backend.service.TransactionService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping
	public List<TransactionResponse> findTransactions(@AuthenticationPrincipal Jwt jwt) {
		return transactionService.findForUser(jwt.getSubject());
	}
}
