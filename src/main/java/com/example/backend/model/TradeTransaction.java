package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TradeTransaction {

	public enum Type {
		BUY,
		SELL
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_user"))
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stock_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_stock"))
	private Stock stock;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private Type type;

	@Column(nullable = false)
	private long quantity;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal price;

	@Column(name = "total_value", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalValue;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected TradeTransaction() {
	}

	public TradeTransaction(AppUser user, Stock stock, Type type, long quantity, BigDecimal price,
			BigDecimal totalValue) {
		this.user = user;
		this.stock = stock;
		this.type = type;
		this.quantity = quantity;
		this.price = price;
		this.totalValue = totalValue;
	}

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public Stock getStock() {
		return stock;
	}

	public Type getType() {
		return type;
	}

	public long getQuantity() {
		return quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
