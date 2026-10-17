package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 12)
	private String symbol;

	@Column(name = "company_name", nullable = false, length = 120)
	private String companyName;

	@Column(name = "current_price", nullable = false, precision = 19, scale = 2)
	private BigDecimal currentPrice;

	protected Stock() {
	}

	public Stock(String symbol, String companyName, BigDecimal currentPrice) {
		this.symbol = symbol;
		this.companyName = companyName;
		this.currentPrice = currentPrice;
	}

	public Long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getCompanyName() {
		return companyName;
	}

	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}
}
