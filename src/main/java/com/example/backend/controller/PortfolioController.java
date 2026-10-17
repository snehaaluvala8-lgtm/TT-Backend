package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.PortfolioResponse;
import com.example.backend.service.PortfolioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

	private final PortfolioService portfolioService;

	public PortfolioController(PortfolioService portfolioService) {
		this.portfolioService = portfolioService;
	}

	@GetMapping
	public PortfolioResponse getPortfolio(@AuthenticationPrincipal Jwt jwt) {
		return portfolioService.getPortfolio(jwt.getSubject());
	}
}
